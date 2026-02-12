package ir.farzadafi.service;

import ir.farzadafi.analyzer.DockerSecurityAnalyzer;
import ir.farzadafi.dto.DockerAnalyzeResponseDto;
import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.mapper.DockerAnalyzeMapper;
import ir.farzadafi.mapper.DockerInstructionMapper;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.parser.DockerfileParser;
import ir.farzadafi.report.DockerAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DockerfileAnalyzerService {
    private final DockerfileParser parser;
    private final DockerInstructionMapper mapper;
    private final DockerSecurityAnalyzer analyzer;

    public List<DockerAnalyzeResponseDto> analyzeDockerfile(String dockerfilePath) {
        List<Map<String, Object>> issues = new ArrayList<>();
        List<DockerInstruction> rawInstructions = parser.parseDockerfile(dockerfilePath);
        List<SemanticDockerInstruction> semanticInstructions = mapToSemanticInstructions(rawInstructions);
        DockerAnalysisReport analyze = analyzer.analyze(semanticInstructions);
        return DockerAnalyzeMapper.mapToResponse(analyze);
    }

    private List<SemanticDockerInstruction> mapToSemanticInstructions(
            List<DockerInstruction> rawInstructions) {
        return rawInstructions.stream()
                .map(mapper::map)
                .toList();
    }
}
