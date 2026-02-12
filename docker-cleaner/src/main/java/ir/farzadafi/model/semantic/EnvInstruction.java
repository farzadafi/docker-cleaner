package ir.farzadafi.model.semantic;

public record EnvInstruction(
        String key,
        String value,
        int line
) implements SemanticDockerInstruction {
}
