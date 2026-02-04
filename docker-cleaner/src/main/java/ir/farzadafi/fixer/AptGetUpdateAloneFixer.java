package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AptGetUpdateAloneFixer implements DockerSmellFixer {

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> result = new ArrayList<>();
        for (SemanticDockerInstruction ins : instructions) {
            if (shouldRemoveInstruction(ins))
                continue;
            if (shouldFixInstruction(ins)) {
                result.add(fixInstruction(ins));
                continue;
            }
            result.add(ins);
        }
        return result;
    }

    private boolean shouldRemoveInstruction(SemanticDockerInstruction ins) {
        return isAptGetUpdateOnly(ins);
    }

    private boolean shouldFixInstruction(SemanticDockerInstruction ins) {
        return isAptGetInstall(ins);
    }

    private SemanticDockerInstruction fixInstruction(SemanticDockerInstruction ins) {
        return addUpdateBeforeInstall((RunInstruction) ins);
    }

    private boolean isAptGetUpdateOnly(SemanticDockerInstruction ins) {
        RunInstruction run = extractRunInstruction(ins);
        if (run == null)
            return false;
        return isAptGetCommand(run) && isUpdateOnly(run);
    }

    private boolean isAptGetInstall(SemanticDockerInstruction ins) {
        RunInstruction run = extractRunInstruction(ins);
        if (run == null)
            return false;
        return isAptGetCommand(run) && containsInstall(run);
    }

    private RunInstruction extractRunInstruction(SemanticDockerInstruction ins) {
        if (ins instanceof RunInstruction run)
            return run;
        return null;
    }

    private boolean isAptGetCommand(RunInstruction run) {
        return run.executable().equals("apt-get");
    }

    private boolean isUpdateOnly(RunInstruction run) {
        List<String> args = run.arguments();
        return args.size() == 1 && args.getFirst().equals("update");
    }

    private boolean containsInstall(RunInstruction run) {
        return run.arguments().contains("install");
    }

    private RunInstruction addUpdateBeforeInstall(RunInstruction run) {
        if (alreadyHasUpdate(run))
            return run;
        return buildUpdatedRun(run);
    }

    private boolean alreadyHasUpdate(RunInstruction run) {
        return run.arguments().contains("update");
    }

    private RunInstruction buildUpdatedRun(RunInstruction run) {
        List<String> newArgs = new ArrayList<>();
        newArgs.add("update");
        newArgs.add("&&");
        newArgs.add("apt-get");
        newArgs.addAll(run.arguments());
        return new RunInstruction(
                run.executable(),
                newArgs,
                run.line()
        );
    }
}
