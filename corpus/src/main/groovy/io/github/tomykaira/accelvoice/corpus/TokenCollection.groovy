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
                    occurrence[it] =
                        (occurrence[it]?:0) + 1
                }
            }
        }
    }

    private static def c(String s) {
        (s as char) as int
    }

    private static StreamTokenizer createTokenizer(Reader reader) {
        new StreamTokenizer(reader)
        // configure if needed
    }
}
