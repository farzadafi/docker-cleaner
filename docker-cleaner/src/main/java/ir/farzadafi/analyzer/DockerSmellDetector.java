package ir.farzadafi.analyzer;

import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;

import java.util.List;

public interface DockerSmellDetector {
    void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report);
}
