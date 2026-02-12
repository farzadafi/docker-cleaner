package ir.farzadafi.controller;

import ir.farzadafi.service.DockerfileAnalyzerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/dockerfileAnalyze")
public class AnalyzerController {
    private final DockerfileAnalyzerService analyzerService;

    @PostMapping("/analyze/{path}")
    public ResponseEntity<Map<String, Object>> analyzeDockerfile(@PathVariable String path) {
        try {
            Map<String, Object> result = analyzerService.analyzeDockerfile(path);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

}
