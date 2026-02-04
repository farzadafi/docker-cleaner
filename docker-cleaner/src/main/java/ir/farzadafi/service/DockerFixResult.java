package ir.farzadafi.service;

import ir.farzadafi.report.DockerAnalysisReport;

public record DockerFixResult(DockerAnalysisReport reportBeforeFix,
                              String cleanedDockerfilePath) {
}
