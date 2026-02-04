package ir.farzadafi.report;

import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DockerAnalysisReport {

    private final List<SmellFinding> findings = new ArrayList<>();

    public void add(SmellFinding finding) {
        findings.add(finding);
    }

    public boolean isClean() {
        return findings.isEmpty();
    }
}