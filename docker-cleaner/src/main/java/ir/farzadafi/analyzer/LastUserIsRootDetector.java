package ir.farzadafi.analyzer;

import ir.farzadafi.analyzer.DockerSmellDetector;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.UserInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LastUserIsRootDetector implements DockerSmellDetector {

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        UserInstruction lastUser = findLastUserInstruction(instructions);
        if (lastUser == null) {
            report.add(new SmellFinding(
                    SmellType.LAST_USER_IS_ROOT,
                    "No USER instruction found. Docker runs as root by default.",
                    getLastLine(instructions)
            ));
            return;
        }

        if (isRootUser(lastUser.user())) {
            report.add(new SmellFinding(
                    SmellType.LAST_USER_IS_ROOT,
                    "Last USER is root. Switch to a non-root user at the end of Dockerfile.",
                    lastUser.line()
            ));
        }
    }

    private UserInstruction findLastUserInstruction(List<SemanticDockerInstruction> instructions) {
        UserInstruction last = null;
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof UserInstruction u)
                last = u;
        }
        return last;
    }

    private boolean isRootUser(String user) {
        if (user == null)
            return true;
        String u = user.trim().toLowerCase();
        return u.equals("root") || u.equals("0");
    }

    private int getLastLine(List<SemanticDockerInstruction> instructions) {
        if (instructions == null || instructions.isEmpty()) return 1;
        return instructions.getLast().line();
    }
}
