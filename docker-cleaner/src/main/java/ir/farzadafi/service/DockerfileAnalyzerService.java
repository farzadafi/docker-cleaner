package ir.farzadafi.service;

import ir.farzadafi.analyzer.DockerSecurityAnalyzer;
import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.mapper.DockerInstructionMapper;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.parser.DockerfileParser;
import ir.farzadafi.report.DockerAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DockerfileAnalyzerService {
    private final DockerfileParser parser;
    private final DockerInstructionMapper mapper;
    private final DockerSecurityAnalyzer analyzer;



    public Map<String, Object> analyzeDockerfile(String dockerfilePath) throws IOException {
//        List<Map<String, Object>> issues = new ArrayList<>();
//        List<DockerInstruction> rawInstructions = parseDockerfile(dockerfilePath);
//        List<SemanticDockerInstruction> semanticInstructions = mapToSemanticInstructions(rawInstructions);
//        DockerAnalysisReport reportBeforeFix = analyzeSecuritySmells(semanticInstructions);
//        List<DockerAnalysisReport> reportBeforeFix1 = analyzeSecuritySmells(semanticInstructions);
//        reportBeforeFix1.forEach(a->{
//            Map<String, Object> issue = new HashMap<>();
//            issue.put("id", 1);
//            issue.put("severity", "high");
//            issue.put("title", "استفاده از latest tag");
//            issue.put("description", "استفاده از تگ latest باعث عدم قطعیت در نسخه ایمیج می‌شود");
////        issue.put("line", 1); // خط تقریبی
//            issue.put("recommendation", "از تگ‌های نسخه‌دار مشخص استفاده کنید");
//            issues.add(issue);
//        });
        Map<String, Object> issue = new HashMap<>();
        issue.put("id", 1);
        issue.put("severity", "high");
        issue.put("title", "استفاده از latest tag");
        issue.put("description", "استفاده از تگ latest باعث عدم قطعیت در نسخه ایمیج می‌شود");
//        issue.put("line", 1); // خط تقریبی
        issue.put("recommendation", "از تگ‌های نسخه‌دار مشخص استفاده کنید");
//        issues.add(issue);
        return issue;
    }

    private List<DockerInstruction> parseDockerfile(String dockerfilePath) {
        return parser.parseDockerfile(dockerfilePath);
    }

    private List<SemanticDockerInstruction> mapToSemanticInstructions(List<DockerInstruction> rawInstructions) {
        return rawInstructions.stream()
                .map(mapper::map)
                .toList();
    }

    private DockerAnalysisReport analyzeSecuritySmells(List<SemanticDockerInstruction> instructions) {
        return analyzer.analyze(instructions);
    }
}
