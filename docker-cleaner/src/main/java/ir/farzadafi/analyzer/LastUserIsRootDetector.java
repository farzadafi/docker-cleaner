package ir.farzadafi.analyzer;

import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.FromInstruction;
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
        if (instructions == null || instructions.isEmpty())
            return;
        int stageStart = findLastStageStartIndex(instructions);
        UserInstruction lastUser = findLastUserInFinalStage(instructions, stageStart);
        if (lastUser == null) {
            report.add(new SmellFinding(
                    SmellType.LAST_USER_IS_ROOT,
                    "No USER instruction found in final stage. Docker runs as root by default.",
                    getLastLine(instructions)
            ));
            return;
        }

        if (isRootUser(lastUser.user())) {
            report.add(new SmellFinding(
                    SmellType.LAST_USER_IS_ROOT,
                    "Last USER in final stage is root. Switch to a non-root user.",
                    lastUser.line()
            ));
        }
    }

    private int findLastStageStartIndex(List<SemanticDockerInstruction> instructions) {
        int lastFromIndex = 0;
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof FromInstruction) {
                lastFromIndex = i;
            }
        }
        return lastFromIndex;
    }

    private UserInstruction findLastUserInFinalStage(List<SemanticDockerInstruction> instructions, int stageStart) {
        UserInstruction last = null;
        for (int i = stageStart; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof UserInstruction u) {
                last = u;
            }
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
        return instructions.getLast().line();
    }
}