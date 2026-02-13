package ir.farzadafi.parser;

import ir.farzadafi.dto.DockerInstruction;
import ir.farzadafi.exception.DockerFileParseException;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class PureJavaDockerfileParser {

    public List<DockerInstruction> parseDockerfile(String dockerfilePath) {
        Path path = Path.of(dockerfilePath);
        String content = readFile(path);
        String[] lines = content.split("\\r?\\n", -1);
        List<RawInstruction> rawInstructions = parseInstructions(lines);
        List<DockerInstruction> result = new ArrayList<>();
        for (RawInstruction raw : rawInstructions) {
            result.add(buildDockerInstruction(raw));
        }
        return result;
    }

    private String readFile(Path path) {
        try { return Files.readString(path, StandardCharsets.UTF_8); }
        catch (IOException e) { throw new DockerFileParseException(e.getMessage()); }
    }

    private List<RawInstruction> parseInstructions(String[] lines) {
        List<RawInstruction> instructions = new ArrayList<>();
        RawInstruction current = null;
        for (String line : lines) {
            if (line == null || line.trim().isEmpty() || line.trim().startsWith("#")) continue;
            if (current != null) { current.fullText.append("\n").append(line);
                if (!lineEndsWithBackslash(line)) { finalizeInstruction(current); instructions.add(current); current = null; }
                continue;
            }
            int ws = firstWhitespace(line); if (ws == -1) continue;
            current = new RawInstruction(line.substring(0, ws).trim());
            current.fullText.append(line);
            if (!lineEndsWithBackslash(line)) { finalizeInstruction(current); instructions.add(current); current = null; }
        }
        if (current != null) { finalizeInstruction(current); instructions.add(current); }
        return instructions;
    }

    private DockerInstruction buildDockerInstruction(RawInstruction raw) {
        String type = raw.keyword.toUpperCase();
        String argsText = raw.argsText == null ? "" : raw.argsText;
        List<Map<String, Object>> tokens = "RUN".equals(type) ? tokenizeRun(argsText) : List.of(tokenMap(argsText, 0, argsText.length()));
        return new DockerInstruction(type, tokens);
    }

    private void finalizeInstruction(RawInstruction raw) {
        String full = raw.fullText.toString();
        if (full.regionMatches(true, 0, raw.keyword, 0, raw.keyword.length()))
            raw.argsText = full.substring(raw.keyword.length()).trim();
        else raw.argsText = full;
    }

    private boolean lineEndsWithBackslash(String line) {
        int end = line.length() - 1; while (end >= 0 && Character.isWhitespace(line.charAt(end))) end--;
        return line.substring(0, end + 1).endsWith("\\");
    }

    private int firstWhitespace(String s) { for (int i = 0; i < s.length(); i++) if (Character.isWhitespace(s.charAt(i))) return i; return -1; }

    private List<Map<String, Object>> tokenizeRun(String input) {
        List<Map<String, Object>> tokens = new ArrayList<>();
        boolean inSingle = false, inDouble = false;
        int tokenStart = -1, i = 0;
        StringBuilder cur = new StringBuilder();
        while (i < input.length()) {
            char ch = input.charAt(i);
            if (!inSingle && !inDouble && Character.isWhitespace(ch)) {
                if (!cur.isEmpty()) { tokens.add(tokenMap(cur.toString(), tokenStart, i)); cur.setLength(0); tokenStart = -1; }
                i++; continue;
            }
            if (tokenStart == -1) tokenStart = i;
            if (!inDouble && ch == '\'') { inSingle = !inSingle; cur.append(ch); i++; continue; }
            if (!inSingle && ch == '"') { inDouble = !inDouble; cur.append(ch); i++; continue; }
            if (ch == '\\') { cur.append('\\'); i++; if (i < input.length()) cur.append(input.charAt(i++)); continue; }
            cur.append(ch); i++;
        }
        if (!cur.isEmpty()) tokens.add(tokenMap(cur.toString(), tokenStart, input.length()));
        return tokens;
    }

    private Map<String, Object> tokenMap(String value, int startChar, int endChar) {
        Map<String, Object> token = new HashMap<>(); token.put("value", value);
        Map<String, Object> range = new HashMap<>();
        Map<String, Object> start = new HashMap<>(); start.put("line", 1); start.put("character", Math.max(0, startChar));
        Map<String, Object> end = new HashMap<>(); end.put("line", 1); end.put("character", Math.max(0, endChar));
        range.put("start", start); range.put("end", end); token.put("range", range);
        return token;
    }

    private static class RawInstruction {
        String keyword; String argsText; StringBuilder fullText = new StringBuilder();
        RawInstruction(String keyword) { this.keyword = keyword; }
    }
}
