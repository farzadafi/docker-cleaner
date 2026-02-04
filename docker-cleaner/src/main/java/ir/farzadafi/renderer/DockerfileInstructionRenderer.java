package ir.farzadafi.renderer;

import ir.farzadafi.model.semantic.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DockerfileInstructionRenderer {

    public String render(SemanticDockerInstruction instruction) {
        if (instruction instanceof RunInstruction(String cmd, List<String> args, int line))
            return renderRun(cmd, args);
        return "";
    }

    private String renderRun(String cmd, List<String> args) {
        if (args == null || args.isEmpty())
            return "RUN " + cmd;
        return "RUN " + cmd + " " + String.join(" ", args);
    }
}