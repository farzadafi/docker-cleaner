package ir.farzadafi.service;

import ir.farzadafi.analyzer.DockerSecurityAnalyzer;
import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.fixer.DockerSmellFixer;
import ir.farzadafi.mapper.DockerInstructionMapper;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.parser.PureJavaDockerfileParser;
import ir.farzadafi.renderer.DockerfileInstructionRenderer;
import ir.farzadafi.report.DockerAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DockerfileAnalyzeAndFixService {
    private final DockerInstructionMapper mapper;
    private final DockerfileInstructionRenderer renderer;
    private final DockerSecurityAnalyzer analyzer;
    private final List<DockerSmellFixer> fixers;
    private final PureJavaDockerfileParser pureJavaDockerfileParser;

    public DockerFixResult analyzeAndFix(String dockerfilePath) throws IOException {
        List<DockerInstruction> rawInstructions = pureJavaDockerfileParser.parseDockerfile(dockerfilePath);
        System.out.println(rawInstructions.get(1));
        List<SemanticDockerInstruction> semanticInstructions = mapToSemanticInstructions(rawInstructions);
        DockerAnalysisReport reportBeforeFix = analyzeSecuritySmells(semanticInstructions);
        if (reportBeforeFix.isClean())
            return buildResult(reportBeforeFix, dockerfilePath);
        List<SemanticDockerInstruction> fixedInstructions = fixAllSmells(semanticInstructions);
        List<String> fixedLines = renderDockerfileLines(fixedInstructions);
        String cleanedPath = saveCleanedDockerfile(dockerfilePath, fixedLines);
        return buildResult(reportBeforeFix, cleanedPath);
    }

    private List<SemanticDockerInstruction> mapToSemanticInstructions(List<DockerInstruction> rawInstructions) {
        return rawInstructions.stream()
                .map(mapper::map)
                .toList();
    }

    private DockerAnalysisReport analyzeSecuritySmells(List<SemanticDockerInstruction> instructions) {
        return analyzer.analyze(instructions);
    }

    private List<SemanticDockerInstruction> fixAllSmells(List<SemanticDockerInstruction> instructions) {
        return applyAllFixers(instructions);
    }

    private List<String> renderDockerfileLines(List<SemanticDockerInstruction> instructions) {
        return instructions.stream()
                .map(renderer::render)
                .filter(this::isValidLine)
                .toList();
    }

    private boolean isValidLine(String line) {
        return line != null && !line.isBlank();
    }

    private String saveCleanedDockerfile(String dockerfilePath, List<String> fixedLines) throws IOException {
        File cleanedFile = createCleanedFilePath(dockerfilePath);
        writeLinesToFile(cleanedFile, fixedLines);
        return cleanedFile.getAbsolutePath();
    }

    private File createCleanedFilePath(String dockerfilePath) {
        File originalFile = new File(dockerfilePath);
        File cleanDir = new File(originalFile.getParentFile(), "clean");
        if (!cleanDir.exists()) {
            cleanDir.mkdirs();
        }
        String cleanedFileName = "cleaned_" + originalFile.getName();
        return new File(cleanDir, cleanedFileName);
    }

    private void writeLinesToFile(File outputFile, List<String> lines) throws IOException {
        try (FileWriter writer = new FileWriter(outputFile)) {
            for (String line : lines) {
                writer.write(line);
                writer.write(System.lineSeparator());
            }
        }
    }

    private DockerFixResult buildResult(DockerAnalysisReport reportBeforeFix, String cleanedPath) {
        return new DockerFixResult(reportBeforeFix, cleanedPath);
    }

    private List<SemanticDockerInstruction> applyAllFixers(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> current = instructions;
        for (DockerSmellFixer fixer : fixers) {
            current = fixer.fix(current);
        }
        return current;
    }
}