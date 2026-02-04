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
            default -> null;
        };
    }

    private RunInstruction mapRun(DockerInstruction raw) {
        List<String> tokens = getTokenValues(raw);
        int line = getLine(raw);
        return new RunInstruction(
                tokens.getFirst(),
                tokens.subList(1, tokens.size()),
                line
        );
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