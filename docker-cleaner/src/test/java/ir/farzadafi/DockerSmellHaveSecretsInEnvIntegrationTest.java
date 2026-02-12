package ir.farzadafi;

import ir.farzadafi.config.DockerTestConfig;
import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import ir.farzadafi.service.DockerFixResult;
import ir.farzadafi.service.DockerfileAnalyzeAndFixService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.fail;

@SpringBootTest
@ActiveProfiles("test")
public class DockerSmellHaveSecretsInEnvIntegrationTest {

    @Autowired
    private DockerfileAnalyzeAndFixService service;

    @Autowired
    private DockerTestConfig config;

    @Test
    void shouldFixHaveSecretsInEnvSmellInAllDockerfiles() {

        String folderPath = config.getDockerfilesFolderPath();
        List<File> dockerfiles = findDockerfiles(folderPath);

        if (dockerfiles.isEmpty()) {
            fail("No Dockerfiles found in folder: " + folderPath);
        }

        int before = 0;
        int after = 0;

        int successFiles = 0;
        int failedFiles = 0;

        List<String> stillSmellyFiles = new ArrayList<>();
        List<String> crashedFiles = new ArrayList<>();

        for (File file : dockerfiles) {
            try {
                DockerFixResult result = service.analyzeAndFix(file.getAbsolutePath());
                int beforeCount = countSmell(result.reportBeforeFix());

                if (beforeCount == 0) {
                    continue;
                }

                DockerFixResult afterResult = service.analyzeAndFix(result.cleanedDockerfilePath());
                int afterCount = countSmell(afterResult.reportBeforeFix());

                before += beforeCount;
                after += afterCount;

                if (afterCount > 0) {
                    stillSmellyFiles.add(file.getAbsolutePath() + " (after=" + afterCount + ")");
                }

                successFiles++;
            } catch (Exception e) {
                failedFiles++;
                crashedFiles.add(file.getAbsolutePath() + " -> " + e.getMessage());
            }
        }

        printSummary(dockerfiles.size(), successFiles, failedFiles, before, after, stillSmellyFiles, crashedFiles);

        if (!crashedFiles.isEmpty()) {
            fail("Some files crashed during processing. Check logs above.");
        }

        if (after > 0) {
            fail("Some Dockerfiles still contain HAVE_SECRETS_IN_ENV smell after fixing. Check logs above.");
        }
    }

    private int countSmell(DockerAnalysisReport report) {
        int count = 0;
        for (SmellFinding finding : report.getFindings()) {
            if (finding.type() == SmellType.HAVE_SECRETS_IN_ENV) {
                count++;
            }
        }
        return count;
    }

    private List<File> findDockerfiles(String folderPath) {
        File root = new File(folderPath);
        List<File> result = new ArrayList<>();
        if (!root.exists() || !root.isDirectory()) {
            return result;
        }
        scanFolder(root, result);
        return result;
    }

    private void scanFolder(File folder, List<File> result) {
        File[] files = folder.listFiles();
        if (files == null) return;
        for (File f : files) {
            if (f.isDirectory()) {
                if (f.getName().equalsIgnoreCase("clean")) {
                    continue;
                }
                scanFolder(f, result);
                continue;
            }
            if (isDockerfile(f)) {
                result.add(f);
            }
        }
    }

    private boolean isDockerfile(File f) {
        String name = f.getName().toLowerCase();
        return name.endsWith(".dockerfile")
                || name.contains("dockerfile");
    }

    private void printSummary(
            int totalFiles,
            int successFiles,
            int failedFiles,
            int before,
            int after,
            List<String> stillSmellyFiles,
            List<String> crashedFiles
    ) {
        System.out.println("======================================");
        System.out.println("Integration Batch Test Finished");
        System.out.println("Total files:   " + totalFiles);
        System.out.println("--------------------------------------");
        System.out.println("HAVE_SECRETS_IN_ENV smells:");
        System.out.println("Before fix: " + before);
        System.out.println("After fix:  " + after);
        System.out.println("Fixed:      " + (before - after));
        System.out.println("======================================");

        if (!stillSmellyFiles.isEmpty()) {
            System.out.println("Files still smelly after fix:");
            for (String f : stillSmellyFiles) {
                System.out.println(" - " + f);
            }
        }

        if (!crashedFiles.isEmpty()) {
            System.out.println("Files crashed during test:");
            for (String f : crashedFiles) {
                System.out.println(" - " + f);
            }
        }
    }
}
