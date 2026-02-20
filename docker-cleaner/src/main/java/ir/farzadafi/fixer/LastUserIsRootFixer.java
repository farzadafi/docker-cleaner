package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.UserInstruction;
import ir.farzadafi.model.semantic.FromInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LastUserIsRootFixer implements DockerSmellFixer {

    private static final String SAFE_USER = "appuser";

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        if (instructions == null || instructions.isEmpty())
            return instructions;
        int stageStart = findLastStageStartIndex(instructions);
        List<SemanticDockerInstruction> result = new ArrayList<>(instructions);
        result.removeIf(i -> i instanceof UserInstruction && result.indexOf(i) >= stageStart);
        int line = getLastLine(result) + 1;
        RunInstruction createUser = new RunInstruction("useradd", List.of("-m", SAFE_USER), line);
        result.add(createUser);
        result.add(new UserInstruction(SAFE_USER, line + 1));
        return result;
    }

    private int findLastStageStartIndex(List<SemanticDockerInstruction> instructions) {
        int lastFromIndex = 0;
        for (int i = 0; i < instructions.size(); i++) {
            if (instructions.get(i) instanceof FromInstruction) {
                lastFromIndex = i;
            }
        }
        return lastFromIndex;
    }

    private int getLastLine(List<SemanticDockerInstruction> instructions) {
        if (instructions == null || instructions.isEmpty()) return 1;
        return instructions.getLast().line();
    }
}