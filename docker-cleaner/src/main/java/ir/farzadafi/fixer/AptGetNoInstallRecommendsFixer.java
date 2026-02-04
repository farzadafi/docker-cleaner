package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AptGetNoInstallRecommendsFixer implements DockerSmellFixer {

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> result = new ArrayList<>();
        for (SemanticDockerInstruction ins : instructions) {
            if (!isAptGetInstall(ins)) {
                result.add(ins);
                continue;
            }
            result.add(addNoInstallRecommendsIfMissing((RunInstruction) ins));
        }
        return result;
    }

    private boolean isAptGetInstall(SemanticDockerInstruction ins) {
        if (!(ins instanceof RunInstruction(String cmd, List<String> args, int line)))
            return false;
        return "apt-get".equals(cmd) && args.contains("install");
    }

    private RunInstruction addNoInstallRecommendsIfMissing(RunInstruction ins) {
        if (alreadyHasNoInstallRecommends(ins))
            return ins;
        return withNoInstallRecommendsInserted(ins);
    }

    private boolean alreadyHasNoInstallRecommends(RunInstruction ins) {
        return ins.arguments().contains("--no-install-recommends");
    }

    private RunInstruction withNoInstallRecommendsInserted(RunInstruction ins) {
        List<String> args = ins.arguments();
        List<String> newArgs = new ArrayList<>();
        for (String arg : args) {
            newArgs.add(arg);
            if ("install".equals(arg))
                newArgs.add("--no-install-recommends");
        }
        return new RunInstruction(ins.executable(), newArgs, ins.line());
    }
}
