package ir.farzadafi.model.semantic;

public sealed interface SemanticDockerInstruction
        permits AddInstruction, CopyInstruction, EnvInstruction, FromInstruction, RunInstruction, UnknownInstruction, UserInstruction {
    int line();
}