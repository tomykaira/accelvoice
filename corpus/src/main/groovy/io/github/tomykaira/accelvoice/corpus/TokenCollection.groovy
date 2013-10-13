package io.github.tomykaira.accelvoice.corpus

/**
 * Collect tokens from given files
 */

class TokenCollection {

    final HashMap<String, Long> occurrence = new HashMap<>();

    void tokenize(String filename) {
        tokenize(new File(filename))
    }

    void tokenize(File file) {
        file.withReader { reader ->
            def tokenizer = createTokenizer(reader)
            runTokenizer(tokenizer)
        }
    }

    private void runTokenizer(StreamTokenizer tokenizer) {
        int code
        while ((code = tokenizer.nextToken()) != StreamTokenizer.TT_EOF) {
            if (code == StreamTokenizer.TT_WORD) {
                tokenizer.sval.split("\\.").each {
                    occurrence.put(it.replaceAll('--', ''), (occurrence.get(it)?:0) + 1)
                }
            }
        }
    }

    private static def c(String s) {
        (s as char) as int
    }

    private static StreamTokenizer createTokenizer(Reader reader) {
        def tokenizer = new StreamTokenizer(reader)
        tokenizer.resetSyntax()
        tokenizer.slashSlashComments(true)
        tokenizer.slashStarComments(true)
        tokenizer.wordChars(c('0'), c('9'))
        tokenizer.wordChars(c('A'), c('Z'))
        tokenizer.wordChars(c('a'), c('z'))
        tokenizer.wordChars(c('_'), c('_'))
        tokenizer.whitespaceChars(c(' '), c(' '))
        tokenizer.whitespaceChars(c('\n'), c('\n'))
        tokenizer.whitespaceChars(c('\t'), c('\t'))
        tokenizer.whitespaceChars(c('\r'), c('\r'))
        tokenizer.quoteChar(c('\''))
        tokenizer.quoteChar(c('\"'))
        tokenizer.parseNumbers()
        tokenizer.eolIsSignificant(true)
        tokenizer
    }
}
