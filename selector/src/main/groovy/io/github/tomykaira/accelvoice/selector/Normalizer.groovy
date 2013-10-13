package io.github.tomykaira.accelvoice.selector

/**
 * Normalize a candidate token to a list of pronounceable words
 */

class Normalizer {
    private final PronouncingDictionary dictionary

    def Normalizer(PronouncingDictionary dictionary) {
        this.dictionary = dictionary
    }

    List<String> normalize(String token) {
        def words = splitCamel(token)
                .collectMany { upperCaseWords(it) }
        if (words.every { dictionary.hasWord(it) })
            words
        else {
            words = splitWordByWord(token.toUpperCase())
            if (words == null)
                throw new NormalizationFailedException(token)
            words.collectMany { upperCaseWords(it) }
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
        int i = 0;
        int length = token.length()
        while (i < length && !dictionary.hasWord(token[0..i]))
            i ++
        if (i == length)
            return null
        def rest = splitWordByWord(token[(i+1)..-1])
        if (rest != null) {
            rest.add(0, token[0..i])
            rest
        } else {
            null
        }
    }

    static List<String> splitCamel(String token) {
        token.split("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[A-Z])")
    }

    static List<String> upperCaseWords(String token) {
        token.split("_").collect { it.toUpperCase() }.findAll { !it.isEmpty() }
    }
}
