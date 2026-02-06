package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.UserInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LastUserIsRootFixer implements DockerSmellFixer {

    private static final String SAFE_USER = "appuser";

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        if (instructions == null || instructions.isEmpty())
            return instructions;
        UserInstruction lastUser = findLastUserInstruction(instructions);
        if (lastUser == null)
            return appendNonRootUserFix(instructions);
        if (isRootUser(lastUser.user()))
            return appendNonRootUserFix(instructions);
        return instructions;
    }

    private List<SemanticDockerInstruction> appendNonRootUserFix(List<SemanticDockerInstruction> instructions) {
        if (alreadyEndsWithNonRootUser(instructions))
            return instructions;
        boolean isAlpine = isAlpineBaseImage(instructions);
        List<SemanticDockerInstruction> result = new ArrayList<>(instructions);
        int line = getLastLine(instructions) + 1;
        RunInstruction createUserCommand = buildCreateUserInstruction(isAlpine, line);
        UserInstruction switchUser = new UserInstruction(SAFE_USER, line + 1);
        result.add(createUserCommand);
        result.add(switchUser);
        return result;
    }

    private RunInstruction buildCreateUserInstruction(boolean isAlpine, int line) {
        if (isAlpine) {
            return new RunInstruction(
                    "adduser",
                    List.of("-D", SAFE_USER),
                    line
            );
        }
        return new RunInstruction(
                "useradd",
                List.of("-m", SAFE_USER),
                line
        );
    }

    private boolean isAlpineBaseImage(List<SemanticDockerInstruction> instructions) {
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof RunInstruction run)
                if (looksLikeApkInstall(run))
                    return true;
        }
        return false;
    }

    private boolean looksLikeApkInstall(RunInstruction run) {
        String cmd = run.executable();
        List<String> args = run.arguments();
        if (cmd == null)
            return false;
        if (cmd.equalsIgnoreCase("apk"))
            return true;
        if (args != null) {
            for (String a : args) {
                if (a != null && a.equalsIgnoreCase("apk"))
                    return true;
            }
        }
        return false;
    }

    private boolean alreadyEndsWithNonRootUser(List<SemanticDockerInstruction> instructions) {
        SemanticDockerInstruction last = instructions.getLast();
        if (!(last instanceof UserInstruction u))
            return false;
        return u.user() != null && u.user().trim().equalsIgnoreCase(SAFE_USER);
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
        if (user == null) return true;
        String u = user.trim().toLowerCase();
        return u.equals("root") || u.equals("0");
    }

    private int getLastLine(List<SemanticDockerInstruction> instructions) {
        if (instructions == null || instructions.isEmpty()) return 1;
        return instructions.getLast().line();
    }
}
