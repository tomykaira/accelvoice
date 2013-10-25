package io.github.tomykaira.accelvoice.projectdict.tokenizer;

import groovy.lang.Closure;
import groovy.lang.Reference;
import org.codehaus.groovy.runtime.DefaultGroovyMethods;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

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
        final List<Character> buf = new ArrayList<Character>();
        for (String pattern : commentPatterns) {
            code = code.replaceAll(pattern, "");
        }
        for (char c : code.toCharArray()) {
            if (buf.isEmpty() && isIdentStart(c) || !buf.isEmpty() && isIdentChar(c)) {
                buf.add(c);
            } else if (!buf.isEmpty()) {
                insert(buf);
            }
        }
        if (!buf.isEmpty()) {
            insert(buf);
        }
    }

    protected void insert(List<Character> buffer) {
        String token = DefaultGroovyMethods.join(buffer, "");
        if (keywords.indexOf(token) == -1) {
            final Integer i = tokens.get(token);
            tokens.put(token, (i == null ? 0 : i) + 1);
        }
        buffer.clear();
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
