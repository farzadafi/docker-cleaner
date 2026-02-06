package ir.farzadafi.model.semantic;

public record AddInstruction(String source,
                             String destination,
                             int line
) implements SemanticDockerInstruction {
}