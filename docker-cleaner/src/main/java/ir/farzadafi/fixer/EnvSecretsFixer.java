package ir.farzadafi.fixer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.semantic.EnvInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.UnknownInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class EnvSecretsFixer implements DockerSmellFixer {

    private static final Set<String> FALSE_POSITIVES = Set.of(
            "GOOGLE_API_KEY",
            "OPENAI_MODEL_NAME",
            "AWS_REGION",
            "PUBLIC_KEY",
            "API_PUBLIC_KEY",
            "MODEL_NAME"
    );

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> result = new ArrayList<>(instructions.size());

        for (SemanticDockerInstruction ins : instructions) {
            if (!(ins instanceof EnvInstruction env)) {
                result.add(ins);
                continue;
            }

            String cleanedKey = cleanKey(env.key());
            String keyUpper = cleanedKey.toUpperCase();
            System.out.println("Processing ENV at line " + env.line() + " | raw key: '" + env.key() + "'");
            System.out.println("Checking if secret: '" + keyUpper + "'");
            System.out.println("Is in set? " + DockerSecretEnvNames.SECRET_ENV_NAMES.contains(keyUpper));

            if (FALSE_POSITIVES.contains(keyUpper)) {
                result.add(ins);
                continue;
            }

            if (DockerSecretEnvNames.SECRET_ENV_NAMES.contains(keyUpper)) {
                String secretId = keyUpper.toLowerCase().replace('_', '-');
                result.add(new UnknownInstruction(
                        "# [AUTO-FIX] Removed secret ENV '" + env.key() +
                                "'. Provide it via: docker build --secret id=" + secretId + ",src=...",
                        env.line()
                ));
            } else {
                result.add(ins);
            }
        }

        return result;
    }

    private String cleanKey(String rawKey) {
        if (rawKey == null) return "";
        return rawKey.trim()
                .replaceAll("^['\"]+", "")
                .replaceAll("['\"]+$", "");
    }
}