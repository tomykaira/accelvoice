package io.github.tomykaira.accelvoice.projectdict.tokenizer;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public abstract class AbstractTokenizer {
    private final List<String> extensions;
    private final List<String> keywords;
    private final List<String> commentPatterns;
    public Map<String, Integer> tokens = new LinkedHashMap<String, Integer>();

    public AbstractTokenizer(List<String> extensions, List<String> keywords, List<String> commentPatterns) {
        this.extensions = extensions;
        this.keywords = keywords;
        this.commentPatterns = commentPatterns;
    }

    public void tokenize(String code) {
        StringBuilder builder = new StringBuilder(32);
        for (String pattern : commentPatterns) {
            code = code.replaceAll(pattern, "");
        }
        for (char c : code.toCharArray()) {
            if (builder.length() == 0 && isIdentStart(c) || builder.length() != 0 && isIdentChar(c)) {
                builder.append(c);
            } else if (builder.length() != 0) {
                insert(builder);

            }
        }
        if (builder.length() != 0) {
            insert(builder);
        }
    }

    protected void insert(StringBuilder builder) {
        String token = builder.toString();
        if (keywords.indexOf(token) == -1) {
            final Integer i = tokens.get(token);
            tokens.put(token, (i == null ? 0 : i) + 1);
        }
        builder.delete(0, token.length());
    }

    protected abstract boolean isIdentStart(char c);

    protected abstract boolean isIdentChar(char c);

    protected static boolean isAlpha(char c) {
        return 'A' <= c && c <= 'Z' || 'a' <= c && c <= 'z';
    }

    protected static boolean isDigit(char c) {
        return '0' <= c && c <= '9';
    }

    public boolean matchExtension(Path path) {
        final String name = path.getFileName().toString();
        for (String extension : extensions) {
            if (name.endsWith(extension))
                return true;
        }
        return false;
    }
}
