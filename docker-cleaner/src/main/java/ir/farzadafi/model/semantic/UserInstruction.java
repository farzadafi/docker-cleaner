package ir.farzadafi.model.semantic;

public record UserInstruction(String user,
                              int line)
        implements SemanticDockerInstruction {
}
