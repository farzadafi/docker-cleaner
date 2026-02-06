package ir.farzadafi.model.semantic;

public record UnknownInstruction(String raw,
                                 int line
) implements SemanticDockerInstruction {
}
