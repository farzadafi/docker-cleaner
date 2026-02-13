package ir.farzadafi.model.semantic;

public record FromInstruction(String image,
                              int line) implements SemanticDockerInstruction {
}
