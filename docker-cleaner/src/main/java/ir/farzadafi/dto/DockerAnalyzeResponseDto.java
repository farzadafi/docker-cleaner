package ir.farzadafi.dto;

public record DockerAnalyzeResponseDto(int line,
                                       String severity,
                                       String title,
                                       String description,
                                       String recommendation) {
}
