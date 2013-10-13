package io.github.tomykaira.accelvoice.selector

/**
 * Normalize a candidate token to a list of pronounceable words
 */

class Normalizer {
    private final PronouncingDictionary dictionary
    private final NormalizationListener listener

    def Normalizer(PronouncingDictionary dictionary, NormalizationListener listener = null) {
        this.dictionary = dictionary
        this.listener = listener
    }

    List<String> normalize(String token) {
        def words
        try {
            words = splitCamel(token)
                    .collectMany { upperCaseWords(it) }
                    .collectMany {
                def result = splitWordByWord(it)
                if (listener != null) {
                    if (result != [it])
                        listener.onUnsureWords(it, result)
                    else
                        listener.onSureWord(token, it)
                }
                result
            }
        } catch (NormalizationFailedException ignored) {
            // rethrow with the original token
            throw new NormalizationFailedException(token)
        }
        if (words.every { dictionary.hasWord(it) }) {
            if (listener != null)
                listener.tokenNormalized(token, words)
            words
        } else {
            throw new NormalizationFailedException(token)
        }
    }

    /**
     * Recursively split words
     * @param token upper case string
     * @return List of tokens, if correct separation is found, null if not
     */
    List<String> splitWordByWord(String token) {
        if (token.isEmpty())
            return []
        if (dictionary.hasWord(token))
            return [token];
        int i = token.length() - 1
        while (i >= 0 && !dictionary.hasWord(token[0..i]))
            i --
        if (i < 0)
            throw new NormalizationFailedException(token)
        def rest = splitWordByWord(token[(i+1)..-1])
        rest.add(0, token[0..i])
        rest
    }

    static List<String> splitCamel(String token) {
        token.split("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[A-Z])")
    }

    static List<String> upperCaseWords(String token) {
        token.split("_").collect { it.toUpperCase() }.findAll { !it.isEmpty() }
    }
}
