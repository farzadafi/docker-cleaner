package ir.farzadafi.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DockerTestConfig {

    @Value("${dockerfiles.folder}")
    private String dockerfilesFolder;

    public String getDockerfilesFolderPath() {
        return dockerfilesFolder;
    }
}
