package ir.farzadafi.analyzer;

import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class DockerSecurityAnalyzer {

    private final List<DockerSmellDetector> detectors;

    public DockerAnalysisReport analyze(List<SemanticDockerInstruction> instructions) {
        DockerAnalysisReport report = new DockerAnalysisReport();
        for (DockerSmellDetector detector : detectors) {
            detector.analyze(instructions, report);
        }
        return report;
    }
}
