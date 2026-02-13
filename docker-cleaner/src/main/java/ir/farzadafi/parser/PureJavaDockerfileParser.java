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
        String content;
        try {
            content = Files.readString(path, StandardCharsets.UTF_8);
        } catch (IOException e) {
            throw new DockerFileParseException(e.getMessage());
        }
        String[] lines = content.split("\\r?\\n", -1);

        List<RawInstruction> rawInstructions = parseRawInstructions(lines);

        List<DockerInstruction> result = new ArrayList<>();

        for (RawInstruction raw : rawInstructions) {
            String type = raw.keyword.toUpperCase();

            // raw.argsText = full args including multiline merged with "\n"
            String argsText = raw.argsText == null ? "" : raw.argsText;

            List<Map<String, Object>> tokens;

            // Tokenize RUN like shell
            if ("RUN".equals(type)) {
                tokens = shellTokenizeWithRange(argsText);
            } else {
                // For other instructions keep it as one token with range
                tokens = new ArrayList<>();
                tokens.add(tokenMap(argsText, 0, argsText.length()));
            }

            result.add(new DockerInstruction(type, tokens));
        }

        return result;
    }

    // ============================================================
    // Part 1) Dockerfile instruction parsing (supports multiline "\")
    // ============================================================

    private List<RawInstruction> parseRawInstructions(String[] lines) {
        List<RawInstruction> instructions = new ArrayList<>();
        RawInstruction current = null;

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];
            if (line == null) continue;

            if (line.trim().isEmpty()) continue;
            if (line.trim().startsWith("#")) continue;

            if (current != null) {
                // We keep newlines because ranges are per-instruction text anyway
                current.fullText.append("\n").append(line);

                if (!endsWithBackslash(line)) {
                    finalizeInstruction(current);
                    instructions.add(current);
                    current = null;
                }
                continue;
            }

            int firstWs = indexOfWhitespace(line);
            if (firstWs == -1) continue;

            String keyword = line.substring(0, firstWs).trim();
            current = new RawInstruction(keyword);
            current.fullText.append(line);

            if (!endsWithBackslash(line)) {
                finalizeInstruction(current);
                instructions.add(current);
                current = null;
            }
        }

        if (current != null) {
            finalizeInstruction(current);
            instructions.add(current);
        }

        return instructions;
    }

    private void finalizeInstruction(RawInstruction raw) {
        String full = raw.fullText.toString();
        String keyword = raw.keyword;

        // remove keyword from beginning (case-insensitive)
        if (full.regionMatches(true, 0, keyword, 0, keyword.length())) {
            String after = full.substring(keyword.length());
            raw.argsText = after.trim();
        } else {
            raw.argsText = full;
        }
    }

    private boolean endsWithBackslash(String line) {
        String trimmedRight = rtrim(line);
        return trimmedRight.endsWith("\\");
    }

    private String rtrim(String s) {
        int end = s.length() - 1;
        while (end >= 0 && Character.isWhitespace(s.charAt(end))) end--;
        return s.substring(0, end + 1);
    }

    private int indexOfWhitespace(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isWhitespace(s.charAt(i))) return i;
        }
        return -1;
    }

    // ============================================================
    // Part 2) RUN tokenizer with range (preserves backslashes)
    // ============================================================

    /**
     * Tokenizes input like shell (roughly):
     * - splits on whitespace (outside quotes)
     * - preserves quotes in token
     * - preserves backslash and the next char
     * <p>
     * Also produces "range" exactly like your old output:
     * range.start.line = 1
     * range.start.character = index in argsText
     */
    private List<Map<String, Object>> shellTokenizeWithRange(String input) {
        List<Map<String, Object>> tokens = new ArrayList<>();

        boolean inSingle = false;
        boolean inDouble = false;

        int i = 0;
        int tokenStart = -1;

        StringBuilder cur = new StringBuilder();

        while (i < input.length()) {
            char ch = input.charAt(i);

            // whitespace split only if not in quotes
            if (!inSingle && !inDouble && Character.isWhitespace(ch)) {
                if (cur.length() > 0) {
                    int tokenEndExclusive = i;
                    tokens.add(tokenMap(cur.toString(), tokenStart, tokenEndExclusive));
                    cur.setLength(0);
                    tokenStart = -1;
                }
                i++;
                continue;
            }

            // starting a new token
            if (tokenStart == -1) tokenStart = i;

            // toggle single quote
            if (!inDouble && ch == '\'') {
                inSingle = !inSingle;
                cur.append(ch);
                i++;
                continue;
            }

            // toggle double quote
            if (!inSingle && ch == '"') {
                inDouble = !inDouble;
                cur.append(ch);
                i++;
                continue;
            }

            // preserve backslash + next char
            if (ch == '\\') {
                cur.append('\\');
                i++;

                if (i < input.length()) {
                    cur.append(input.charAt(i));
                    i++;
                }
                continue;
            }

            cur.append(ch);
            i++;
        }

        // last token
        if (cur.length() > 0) {
            tokens.add(tokenMap(cur.toString(), tokenStart, input.length()));
        }

        return tokens;
    }

    // ============================================================
    // Token map builder (value + range)
    // ============================================================

    private Map<String, Object> tokenMap(String value, int startChar, int endCharExclusive) {
        Map<String, Object> token = new HashMap<>();
        token.put("value", value);

        // range object
        Map<String, Object> range = new HashMap<>();

        Map<String, Object> start = new HashMap<>();
        start.put("line", 1);
        start.put("character", Math.max(0, startChar));

        Map<String, Object> end = new HashMap<>();
        end.put("line", 1);
        end.put("character", Math.max(0, endCharExclusive));

        range.put("start", start);
        range.put("end", end);

        token.put("range", range);

        return token;
    }

    // ============================================================
    // Internal
    // ============================================================

    private static class RawInstruction {
        String keyword;
        String argsText;
        StringBuilder fullText = new StringBuilder();

        RawInstruction(String keyword) {
            this.keyword = keyword;
        }
    }
}
