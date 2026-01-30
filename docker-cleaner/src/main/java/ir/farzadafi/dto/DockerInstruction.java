package ir.farzadafi.dto;

import java.util.List;

public record DockerInstruction(String type,
                                List<Object> value) {
}