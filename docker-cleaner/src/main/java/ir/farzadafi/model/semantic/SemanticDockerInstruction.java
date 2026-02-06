package ir.farzadafi.model.semantic;

public sealed interface SemanticDockerInstruction
        permits AddInstruction,
        CopyInstruction,
        RunInstruction,
        UnknownInstruction {
    int line();
}