package ir.farzadafi.fixer;

import ir.farzadafi.constants.DockerSecretEnvNames;
import ir.farzadafi.model.semantic.EnvInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.UnknownInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class EnvSecretsFixer implements DockerSmellFixer {

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {

        List<SemanticDockerInstruction> result = new ArrayList<>();

        for (SemanticDockerInstruction ins : instructions) {

            if (!(ins instanceof EnvInstruction env)) {
                result.add(ins);
                continue;
            }

            if (!isSecretKey(env.key())) {
                result.add(ins);
                continue;
            }

            result.add(new UnknownInstruction(
                    "# [AUTO-FIX] Removed secret ENV '" + env.key() + "'. Provide it via: docker build --secret",
                    env.line()
            ));
        }

        return result;
    }

    private boolean isSecretKey(String key) {
        if (key == null) return false;
        return DockerSecretEnvNames.SECRET_ENV_NAMES.contains(key.trim().toUpperCase());
    }
}
