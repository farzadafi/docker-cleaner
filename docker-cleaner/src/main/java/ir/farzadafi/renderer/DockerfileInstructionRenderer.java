package ir.farzadafi.renderer;

import ir.farzadafi.model.semantic.*;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class DockerfileInstructionRenderer {

    public String render(SemanticDockerInstruction instruction) {
        if (instruction instanceof RunInstruction(String cmd, List<String> args, int line))
            return renderRun(cmd, args);
        if (instruction instanceof AddInstruction(String src, String dest, int line))
            return renderAdd(src, dest);
        if (instruction instanceof CopyInstruction(String src, String dest, int line))
            return renderCopy(src, dest);
        if (instruction instanceof UnknownInstruction(String raw, int line))
            return raw;
        if (instruction instanceof UserInstruction user)
            return renderUser(user.user());
        return "";
    }

    private String renderRun(String cmd, List<String> args) {
        if (cmd == null || cmd.isBlank())
            return "";
        if (args == null || args.isEmpty())
            return "RUN " + cmd;
        return "RUN " + cmd + " " + String.join(" ", args);
    }

    private String renderAdd(String src, String dest) {
        if (src == null || src.isBlank() || dest == null || dest.isBlank())
            return "";
        return "ADD " + src + " " + dest;
    }

    private String renderCopy(String src, String dest) {
        if (src == null || src.isBlank() || dest == null || dest.isBlank())
            return "";
        return "COPY " + src + " " + dest;
    }

    private String renderUser(String user) {
        return "USER " + user;
    }
}