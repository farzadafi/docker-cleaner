package ir.farzadafi.mapper;

import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.model.semantic.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class DockerInstructionMapper {

    public SemanticDockerInstruction map(DockerInstruction raw) {
        return switch (raw.type()) {
            case "RUN" -> mapRun(raw);
            case "ADD" -> mapAdd(raw);
            case "USER" -> mapUser(raw);
            case "COPY" -> mapCopy(raw);
            default -> mapUnknown(raw);
        };
    }

    private RunInstruction mapRun(DockerInstruction raw) {
        List<String> tokens = getTokenValues(raw);
        int line = getLine(raw);
        if (tokens.isEmpty())
            return new RunInstruction("", List.of(), line);
        if (tokens.size() == 1)
            return new RunInstruction(tokens.getFirst(), List.of(), line);
        return new RunInstruction(
                tokens.getFirst(),
                tokens.subList(1, tokens.size()),
                line
        );
    }

    private AddInstruction mapAdd(DockerInstruction raw) {
        List<String> tokens = getTokenValues(raw);
        int line = getLine(raw);
        if (tokens.size() < 2)
            return new AddInstruction("", "", line);
        return new AddInstruction(tokens.get(0), tokens.get(1), line);
    }

    private CopyInstruction mapCopy(DockerInstruction raw) {
        List<String> tokens = getTokenValues(raw);
        int line = getLine(raw);
        if (tokens.size() < 2)
            return new CopyInstruction("", "", line);
        return new CopyInstruction(tokens.get(0), tokens.get(1), line);
    }

    private UserInstruction mapUser(DockerInstruction raw) {
        List<String> tokens = getTokenValues(raw);
        int line = getLine(raw);
        String user = tokens.isEmpty() ? "" : tokens.getFirst();
        return new UserInstruction(user, line);
    }

    private UnknownInstruction mapUnknown(DockerInstruction raw) {
        int line = 0;
        try {
            line = getLine(raw);
        } catch (Exception ignored) {
        }
        return new UnknownInstruction(raw.type(), line);
    }

    private List<String> getTokenValues(DockerInstruction raw) {
        return raw.value().stream()
                .map(v -> v.get("value").toString())
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    private int getLine(DockerInstruction raw) {
        Map<String, Object> range = (Map<String, Object>) raw.value().getFirst().get("range");
        Map<String, Object> start = (Map<String, Object>) range.get("start");
        return (int) start.get("line");
    }
}