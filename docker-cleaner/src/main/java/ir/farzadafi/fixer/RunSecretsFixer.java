package ir.farzadafi.fixer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.UnknownInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class RunSecretsFixer implements DockerSmellFixer {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$([A-Za-z_][A-Za-z0-9_]*)|\\$\\{([A-Za-z_][A-Za-z0-9_]*)\\}");

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> result = new ArrayList<>();

        for (SemanticDockerInstruction ins : instructions) {
            if (!(ins instanceof RunInstruction run)) {
                result.add(ins);
                continue;
            }

            String fullCommand = buildFullCommand(run);
            if (fullCommand.isBlank()) {
                result.add(ins);
                continue;
            }

            boolean hasSecret = false;
            StringBuilder comment = new StringBuilder("# [SECURITY] RUN command uses potential secret variable(s):\n");

            Matcher matcher = VAR_PATTERN.matcher(fullCommand);
            while (matcher.find()) {
                String varName = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
                String upper = varName.toUpperCase();

                if (DockerSecretEnvNames.SECRET_ENV_NAMES.contains(upper)) {
                    hasSecret = true;
                    comment.append("#   - $").append(varName).append("\n");
                }
            }

            if (hasSecret) {
                comment.append("# → Replace with --mount=type=secret,id=... and read from /run/secrets/\n");
                comment.append("# Original RUN: ").append(fullCommand);

                result.add(new UnknownInstruction(comment.toString(), run.line()));
            } else {
                result.add(ins);
            }
        }

        return result;
    }

    private String buildFullCommand(RunInstruction run) {
        String argsPart = String.join(" ", run.arguments());
        return (run.executable() + " " + argsPart).trim();
    }
}