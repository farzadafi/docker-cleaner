package ir.farzadafi.model.semantic;

public sealed interface SemanticDockerInstruction permits RunInstruction {
    int line();
}