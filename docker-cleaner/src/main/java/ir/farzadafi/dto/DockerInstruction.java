package ir.farzadafi.dto;

import java.util.List;
import java.util.Map;

public record DockerInstruction(String type,
                                List<Map<String, Object>> value) {
}