package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AddInsteadOfWgetOrCopyFixer implements DockerSmellFixer {

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        boolean hasAnyAddUrl = containsAnyAddUrl(instructions);
        boolean wgetAlreadyInstalled = containsWgetInstall(instructions);
        boolean wgetInstallInserted = false;
        List<SemanticDockerInstruction> result = new ArrayList<>();
        for (SemanticDockerInstruction ins : instructions) {
            if (!(ins instanceof AddInstruction(String source, String destination, int line))) {
                result.add(ins);
                continue;
            }
            if (isUrl(source)) {
                if (hasAnyAddUrl && !wgetAlreadyInstalled && !wgetInstallInserted) {
                    result.add(buildWgetInstallRunInstruction(line));
                    wgetInstallInserted = true;
                }
                result.add(buildWgetRunInstruction(source, destination, line));
                continue;
            }
            result.add(new CopyInstruction(source, destination, line));
        }
        return result;
    }

    private boolean containsAnyAddUrl(List<SemanticDockerInstruction> instructions) {
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof AddInstruction add)
                if (isUrl(add.source()))
                    return true;
        }
        return false;
    }

    private boolean isUrl(String src) {
        if (src == null) return false;
        String s = src.toLowerCase();
        return s.startsWith("http://") || s.startsWith("https://");
    }

    private boolean containsWgetInstall(List<SemanticDockerInstruction> instructions) {
        for (SemanticDockerInstruction ins : instructions) {
            if (!(ins instanceof RunInstruction run))
                continue;
            String executable = run.executable();
            List<String> args = run.arguments();
            if (executable == null) continue;
            if (args == null || args.isEmpty()) continue;
            String full = (executable + " " + String.join(" ", args)).toLowerCase();
            // Debian/Ubuntu
            if (full.contains("apt-get install") && full.contains("wget"))
                return true;
            // Alpine
            if (full.contains("apk add") && full.contains("wget"))
                return true;
            // CentOS/RHEL
            if (full.contains("yum install") && full.contains("wget"))
                return true;
        }
        return false;
    }

    private RunInstruction buildWgetInstallRunInstruction(int line) {
        return new RunInstruction(
                "apt-get",
                List.of(
                        "update",
                        "&&",
                        "apt-get",
                        "install",
                        "--no-install-recommends",
                        "-y",
                        "wget"
                ),
                line
        );
    }

    private RunInstruction buildWgetRunInstruction(String url, String destination, int line) {
        return new RunInstruction(
                "wget",
                List.of(
                        "-O",
                        destination,
                        url
                ),
                line
        );
    }
}