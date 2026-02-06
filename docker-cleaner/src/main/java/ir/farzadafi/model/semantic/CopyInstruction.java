package ir.farzadafi.model.semantic;

public record CopyInstruction(String source,
                              String destination,
                              int line
) implements SemanticDockerInstruction {
}
