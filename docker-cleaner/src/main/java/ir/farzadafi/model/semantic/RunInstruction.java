package ir.farzadafi.model.semantic;

import java.util.List;

public record RunInstruction(String executable,
                             List<String> arguments,
                             int line
) implements SemanticDockerInstruction {
}