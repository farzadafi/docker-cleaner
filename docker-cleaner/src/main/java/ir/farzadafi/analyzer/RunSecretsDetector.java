package ir.farzadafi.analyzer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Component
public class RunSecretsDetector implements DockerSmellDetector {

    private static final Pattern VAR_PATTERN = Pattern.compile(
            "\\$([A-Za-z_][A-Za-z0-9_]*)|\\$\\{([A-Za-z_][A-Za-z0-9_]*)}|\"\\$\\{?([A-Za-z_][A-Za-z0-9_]*)}?\""
    );

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof RunInstruction run) {
                analyzeRun(run, report);
            }
        }
    }

    private void analyzeRun(RunInstruction run, DockerAnalysisReport report) {
        String fullCommand = buildFullCommand(run);
        if (fullCommand.isBlank()) {
            return;
        }

        Matcher matcher = VAR_PATTERN.matcher(fullCommand);
        while (matcher.find()) {
            String varName = matcher.group(1) != null ? matcher.group(1) :
                    matcher.group(2) != null ? matcher.group(2) :
                            matcher.group(3);
            String upper = varName.toUpperCase();

            if (DockerSecretEnvNames.SECRET_ENV_NAMES.contains(upper)) {
                report.add(new SmellFinding(
                        SmellType.HAVE_SECRETS_IN_RUN,
                        "Potential secret in RUN: $" + varName +
                                " → use --mount=type=secret,id=" +
                                upper.toLowerCase().replace('_', '-') + " instead",
                        run.line()
                ));
            }
        }
    }

    private String buildFullCommand(RunInstruction run) {
        String argsPart = String.join(" ", run.arguments());
        return (run.executable() + " " + argsPart).trim();
    }
}