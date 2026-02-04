package ir.farzadafi.analyzer;

import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AptGetUpdateAloneDetector implements DockerSmellDetector {

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            analyzeInstruction(ins, report);
        }
    }

    private void analyzeInstruction(SemanticDockerInstruction ins, DockerAnalysisReport report) {
        if (ins instanceof RunInstruction(String cmd, List<String> args, int line))
            analyzeRun(cmd, args, line, report);
    }

    private void analyzeRun(String cmd, List<String> args, int line, DockerAnalysisReport report) {
        if (isAptGetUpdateAlone(cmd, args))
            reportAptGetUpdateAlone(line, report);
    }

    private boolean isAptGetUpdateAlone(String cmd, List<String> args) {
        return cmd.equals("apt-get") && args.contains("update") && !args.contains("install");
    }

    private void reportAptGetUpdateAlone(int line, DockerAnalysisReport report) {
        SmellFinding smellFinding = new SmellFinding(SmellType.APT_GET_UPDATE_ALONE,
                "Use apt-get update together with install (avoid cached update layer)",
                line);
        report.add(smellFinding);
    }
}