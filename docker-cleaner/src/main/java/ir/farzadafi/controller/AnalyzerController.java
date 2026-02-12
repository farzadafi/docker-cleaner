package ir.farzadafi.controller;

import ir.farzadafi.dto.DockerAnalyzeResponseDto;
import ir.farzadafi.service.DockerfileAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dockerfile-analyze")
public class AnalyzerController {
    private static final String UPLOAD_DIR = System.getProperty("user.home") + "/docker-cleaner";
    private final DockerfileAnalyzerService analyzerService;

    @PostMapping("/analyze")
    public List<DockerAnalyzeResponseDto> analyzeDockerfile(@RequestParam("file") MultipartFile file) throws Exception {
        String savedPath = saveFileInUserHome(file);
        return analyzerService.analyzeDockerfile(savedPath);
    }

    private String saveFileInUserHome(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File is empty!");
        Path uploadDir = Paths.get(UPLOAD_DIR);
        Files.createDirectories(uploadDir);
        String originalName = file.getOriginalFilename();
        String symbolRegex = "[^a-zA-Z0-9._-]";
        String safeName = (originalName == null ? "dockerfile" : originalName.replaceAll(symbolRegex, "_"));
        Path finalFile = uploadDir.resolve(safeName);
        Files.write(finalFile, file.getBytes());
        return finalFile.toString();
    }
}
