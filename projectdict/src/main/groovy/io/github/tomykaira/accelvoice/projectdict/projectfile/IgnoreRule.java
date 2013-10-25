package io.github.tomykaira.accelvoice.projectdict.projectfile;

public class IgnoreRule {
    private final boolean absolute;
    private final String pattern;

    public IgnoreRule(String rule) {
        if (rule.charAt(0) == '/') {
            absolute = true;
            pattern = rule.substring(1);
        } else {
            absolute = false;
            pattern = rule;
        }
    }

    /**
     * Match the given path and pattern
     * @param relativePath path relative to the project root
     * @return true if the file should be ignored
     */
    public boolean match(String relativePath) {
        if (absolute) {
            return relativePath.startsWith(pattern);
        } else {
            return relativePath.contains(pattern);
        }
    }
}
