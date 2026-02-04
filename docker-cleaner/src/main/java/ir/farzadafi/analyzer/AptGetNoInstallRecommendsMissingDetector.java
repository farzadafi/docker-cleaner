package ir.farzadafi.analyzer;

import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AptGetNoInstallRecommendsMissingDetector implements DockerSmellDetector {

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            if (!isAptGetInstall(ins))
                continue;
            if (hasNoInstallRecommends(ins))
                continue;
            int line = ins.line();
            report.add(new SmellFinding(
                    SmellType.APT_GET_NO_INSTALL_RECOMMENDS_MISSING,
                    "Avoid additional packages by specifying `--no-install-recommends` in apt-get install",
                    line
            ));
        }
    }

    private boolean isAptGetInstall(SemanticDockerInstruction ins) {
        if (!(ins instanceof RunInstruction(String cmd, List<String> args, int line)))
            return false;
        return "apt-get".equals(cmd) && args.contains("install");
    }

    private boolean hasNoInstallRecommends(SemanticDockerInstruction ins) {
        RunInstruction run = (RunInstruction) ins;
        return run.arguments().contains("--no-install-recommends");
    }
}
