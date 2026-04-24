package ir.farzadafi.analyzer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.EnvInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Component
public class EnvSecretsDetector implements DockerSmellDetector {

    private static final Set<String> FALSE_POSITIVES = Set.of(
            "GOOGLE_API_KEY",
            "OPENAI_MODEL_NAME",
            "AWS_REGION",
            "PUBLIC_KEY",
            "API_PUBLIC_KEY",
            "MODEL_NAME"
    );

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof EnvInstruction env) {
                analyzeEnv(env, report);
            }
        }
    }

    private void analyzeEnv(EnvInstruction env, DockerAnalysisReport report) {
        System.out.println("Processing ENV at line " + env.line() + " | raw key: '" + env.key() + "'");
        String cleanedKey = cleanKey(env.key());
        String keyUpper = cleanedKey.toUpperCase();
        System.out.println("Checking if secret: '" + keyUpper + "'");
        System.out.println("Is in set? " + DockerSecretEnvNames.SECRET_ENV_NAMES.contains(keyUpper));

        if (FALSE_POSITIVES.contains(keyUpper)) {
            return;
        }
        if (DockerSecretEnvNames.SECRET_ENV_NAMES.contains(keyUpper)) {
            report.add(new SmellFinding(
                    SmellType.HAVE_SECRETS_IN_ENV,
                    "Secret found in ENV: " + env.key() + " → should be provided via docker build --secret id=" +
                            keyUpper.toLowerCase().replace('_', '-') + ",src=...",
                    env.line()
            ));
        }
    }

    private String cleanKey(String rawKey) {
        if (rawKey == null) return "";
        return rawKey.trim()
                .replaceAll("^['\"]+", "")
                .replaceAll("['\"]+$", "");
    }
}