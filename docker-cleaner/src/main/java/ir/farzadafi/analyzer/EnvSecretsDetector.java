package ir.farzadafi.analyzer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.EnvInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EnvSecretsDetector implements DockerSmellDetector {

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            analyzeEnv(ins, report);
        }
    }

    private void analyzeEnv(SemanticDockerInstruction ins, DockerAnalysisReport report) {
        if (!(ins instanceof EnvInstruction env)) {
            return;
        }

        if (!isSecretKey(env.key())) {
            return;
        }

        report.add(new SmellFinding(
                SmellType.HAVE_SECRETS_IN_ENV,
                "Secret found in ENV: " + env.key() + " (should be provided via docker build --secret)",
                env.line()
        ));
    }

    private boolean isSecretKey(String key) {
        if (key == null) return false;
        return DockerSecretEnvNames.SECRET_ENV_NAMES.contains(key.trim().toUpperCase());
    }
}
