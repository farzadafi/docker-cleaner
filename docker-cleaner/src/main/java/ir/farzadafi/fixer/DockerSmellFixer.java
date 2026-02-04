package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.SemanticDockerInstruction;

import java.util.List;

public interface DockerSmellFixer {
    List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions);
}