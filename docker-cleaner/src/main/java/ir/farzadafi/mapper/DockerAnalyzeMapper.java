package ir.farzadafi.mapper;

import ir.farzadafi.dto.DockerAnalyzeResponseDto;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;

import java.util.List;

public class DockerAnalyzeMapper {

    private DockerAnalyzeMapper() {
        throw new AssertionError("can't instantiate utility class!");
    }

    public static List<DockerAnalyzeResponseDto> mapToResponse(
            DockerAnalysisReport report) {
        return report.getFindings()
                .stream()
                .map(DockerAnalyzeMapper::mapFinding)
                .toList();
    }

    private static DockerAnalyzeResponseDto mapFinding(SmellFinding finding) {
        SmellType type = finding.type();

        return new DockerAnalyzeResponseDto(
                finding.line(),
                getSeverity(type),
                type.getTitle(),
                finding.message(),
                getRecommendation(type)
        );
    }

    private static String getSeverity(SmellType type) {
        return switch (type) {
            case HAVE_SECRETS_IN_ENV -> "CRITICAL";
            case LAST_USER_IS_ROOT -> "HIGH";
            case ADD_INSTEAD_OF_COPY_OR_WGET -> "MEDIUM";
            case APT_GET_UPDATE_ALONE, APT_GET_NO_INSTALL_RECOMMENDS_MISSING -> "LOW";
        };
    }

    private static String getRecommendation(SmellType type) {
        return switch (type) {
            case APT_GET_UPDATE_ALONE ->
                    "Combine apt-get update with apt-get install in the same RUN command to avoid stale cache issues.";

            case APT_GET_NO_INSTALL_RECOMMENDS_MISSING ->
                    "Use --no-install-recommends to reduce image size and avoid installing unnecessary packages.";

            case ADD_INSTEAD_OF_COPY_OR_WGET ->
                    "Prefer COPY for local files and use curl/wget for remote files instead of ADD.";

            case LAST_USER_IS_ROOT -> "Create and switch to a non-root user for better container security.";

            case HAVE_SECRETS_IN_ENV ->
                    "Do not store secrets in ENV. Use Docker secrets or runtime environment variables instead.";
        };
    }
}
