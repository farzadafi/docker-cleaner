package ir.farzadafi.parser;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.exception.DockerFileParseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class DockerfileParser {

    private final ObjectMapper mapper;

    @Value("${node.parser.path}")
    private String jsScriptPath;

    @Value("${node.working.dir}")
    private String workDir;

    public List<DockerInstruction> parseDockerfile(String dockerfilePath) {
        try {
            String jsonOutput = runNodeParser(jsScriptPath, dockerfilePath);
            CollectionType type = mapper.getTypeFactory()
                    .constructCollectionType(List.class, DockerInstruction.class);
            return mapper.readValue(jsonOutput, type);
        } catch (Exception e) {
            log.error("Parse error", e);
            throw new DockerFileParseException(e.getMessage());
        }
    }

    private String runNodeParser(String scriptPath, String dockerfilePath) throws Exception {
        Process process = startNodeProcess(scriptPath, dockerfilePath);
        return readProcessOutput(process);
    }

    private Process startNodeProcess(String scriptPath, String dockerfilePath) throws IOException {
        ProcessBuilder pb = new ProcessBuilder("node", scriptPath, dockerfilePath);
        pb.directory(new File(workDir));
        pb.redirectErrorStream(true);
        return pb.start();
    }

    private String readProcessOutput(Process process) throws IOException, InterruptedException {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            StringBuilder output = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line);
            }
            process.waitFor();
            return output.toString();
        }
    }
}