package ir.farzadafi.analyzer;

import ir.farzadafi.model.enumeration.SmellType;
import ir.farzadafi.model.semantic.AddInstruction;
import ir.farzadafi.model.semantic.SemanticDockerInstruction;
import ir.farzadafi.report.DockerAnalysisReport;
import ir.farzadafi.report.SmellFinding;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AddInsteadOfCopyOrWgetDetector implements DockerSmellDetector {

    @Override
    public void analyze(List<SemanticDockerInstruction> instructions, DockerAnalysisReport report) {
        for (SemanticDockerInstruction ins : instructions) {
            if (ins instanceof AddInstruction add) {
                report.add(new SmellFinding(
                        SmellType.ADD_INSTEAD_OF_COPY_OR_WGET,
                        buildMessage(add.source()),
                        add.line()
                ));
            }
        }
    }

    private String buildMessage(String src) {
        if (isUrl(src))
            return "Avoid ADD for URLs. Use RUN wget/curl instead.";
        return "Avoid ADD. Use COPY for local files.";
    }

    private boolean isUrl(String src) {
        if (src == null)
            return false;
        String lowerCaseSrc = src.toLowerCase();
        return lowerCaseSrc.startsWith("http://") || lowerCaseSrc.startsWith("https://");
    }
}
