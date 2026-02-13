package ir.farzadafi.fixer;

import ir.farzadafi.model.semantic.RunInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AptGetUpdateAloneFixer implements DockerSmellFixer {

    @Override
    public List<SemanticDockerInstruction> fix(List<SemanticDockerInstruction> instructions) {
        List<SemanticDockerInstruction> result = new ArrayList<>();
        for (SemanticDockerInstruction ins : instructions) {
            if (shouldRemoveInstruction(ins))
                continue;
            if (shouldFixInstruction(ins))
                result.add(fixAptGetRun((RunInstruction) ins));
            else
                result.add(ins);
        }
        return result;
    }

    private boolean shouldRemoveInstruction(SemanticDockerInstruction ins) {
        RunInstruction run = extractRun(ins);
        if (run == null) return false;
        return isAptGet(run) && isOnlyUpdate(run);
    }

    private boolean shouldFixInstruction(SemanticDockerInstruction ins) {
        RunInstruction run = extractRun(ins);
        if (run == null) return false;
        return isAptGet(run) && hasInstall(run.arguments());
    }

    private RunInstruction fixAptGetRun(RunInstruction run) {
        List<String> args = run.arguments();
        List<String> subCommands = splitByAnd(args);
        boolean hasUpdate = subCommands.stream().anyMatch(this::isUpdateSub);
        boolean updateIsFirst = !subCommands.isEmpty() && isUpdateSub(subCommands.get(0));
        List<String> fixedSubCommands = new ArrayList<>();
        if (hasUpdate && updateIsFirst) {
            for (String sub : subCommands) {
                fixedSubCommands.add(fixTypoInSubCommand(sub));
            }
        } else if (hasUpdate) {
            String updateSub = subCommands.stream().filter(this::isUpdateSub).findFirst().orElse("apt-get update");
            List<String> otherSubs = subCommands.stream()
                    .filter(s -> !isUpdateSub(s))
                    .map(this::fixTypoInSubCommand)
                    .toList();
            fixedSubCommands.add(updateSub);
            fixedSubCommands.addAll(otherSubs);
        } else {
            fixedSubCommands.add("apt-get update");
            for (String sub : subCommands) {
                fixedSubCommands.add(fixTypoInSubCommand(sub));
            }
        }

        fixedSubCommands = fixedSubCommands.stream()
                .filter(this::isValidSubCommand)
                .collect(Collectors.toList());
        if (fixedSubCommands.isEmpty())
            return null;
        List<String> newArgs = new ArrayList<>();
        for (int i = 0; i < fixedSubCommands.size(); i++) {
            String fixedSub = fixedSubCommands.get(i).trim();
            String[] parts = fixedSub.split("\\s+");
            if (i > 0)
                newArgs.add("&&");
            if (i == 0 && parts.length > 0 && "apt-get".equals(parts[0]))
                newArgs.addAll(Arrays.asList(parts).subList(1, parts.length));
            else
                newArgs.addAll(Arrays.asList(parts));
        }
        return new RunInstruction(run.executable(), newArgs, run.line());
    }

    private List<String> splitByAnd(List<String> args) {
        List<String> subCommands = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String token : args) {
            if ("&&".equals(token)) {
                if (!current.isEmpty()) {
                    subCommands.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                if (!current.isEmpty()) current.append(" ");
                current.append(token);
            }
        }
        if (!current.isEmpty()) {
            subCommands.add(current.toString().trim());
        }
        return subCommands;
    }

    private boolean isAptGet(RunInstruction run) {
        return "apt-get".equals(run.executable());
    }

    private boolean isOnlyUpdate(RunInstruction run) {
        List<String> a = run.arguments();
        return a.size() == 1 && "update".equals(a.get(0));
    }

    private boolean hasInstall(List<String> args) {
        return args.contains("install");
    }

    private boolean isUpdateSub(String sub) {
        String trimmed = sub.trim().toLowerCase();
        return trimmed.equals("update") || trimmed.equals("apt-get update");
    }

    private String fixTypoInSubCommand(String sub) {
        String trimmed = sub.trim();
        if (!trimmed.startsWith("apt-get") && (trimmed.startsWith("install") || trimmed.startsWith("update"))) {
            return "apt-get " + trimmed;
        }
        return trimmed;
    }

    private boolean isValidSubCommand(String sub) {
        String trimmed = sub.trim();
        if (trimmed.isEmpty()) return false;
        return !trimmed.equals("apt-get") && !trimmed.equals("install"); // ناقص
    }

    private RunInstruction extractRun(SemanticDockerInstruction ins) {
        return ins instanceof RunInstruction ? (RunInstruction) ins : null;
    }
}