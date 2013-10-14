package io.github.tomykaira.accelvoice.selector

import groovy.transform.Immutable

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

    NormalizationResult normalize(String token) {
        def unknowns = []
        def words = splitCamel(replaceNumbers(token))
                .collectMany { upperCaseWords(it) }
                .each {
            def isKnown = dictionary.hasWord(it)
            if (listener != null) {
                if (!isKnown)
                    listener.onUnsureWord(it)
                else
                    listener.onSureWord(it)
            }
            if (!isKnown)
                unknowns.add(it)
        }
        if (listener)
            listener.tokenNormalized(token, words)
        new NormalizationResult(words, unknowns)
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
        def length = token.length()
        def table = new WordChain[length + 1];
        table[0] = new WordChain([], 1)
        for (int i = 1; i <= length; i ++) {
            table[i] = new WordChain([], 0)
            for (int j = 0; j < i; j ++) {
                table[i] = max(table[i], table[j].add(token[j..(i-1)]))
            }
        }
        def result = table.last()
        if (result.freq == 0)
            throw new NormalizationFailedException(token)
        else
            table[length].words
    }

    private static def max(WordChain l, WordChain r) {
        if (l.freq > r.freq)
            l
        else
            r
    }

    @Immutable
    private class WordChain {
        List<String> words
        double freq

        def add(String word) {
            new WordChain(words.plus(word), freq * dictionary.ratioOf(word))
        }
    }

    static private def numberTable = [
            '0': 'ZERO',
            '1': 'ONE',
            '2': 'TWO',
            '3': 'THREE',
            '4': 'FOUR',
            '5': 'FIVE',
            '6': 'SIX',
            '7': 'SEVEN',
            '8': 'EIGHT',
            '9': 'NINE'
    ]

    static String replaceNumbers(String token) {
        token.replaceAll("[0-9]") {
            '_' + numberTable[it] + '_'
        }
    }

    static List<String> splitCamel(String token) {
        token.split("(?<=[A-Z])(?=[A-Z][a-z])|(?<=[a-z])(?=[A-Z])")
    }

    static List<String> upperCaseWords(String token) {
        token.split("_").collect { it.toUpperCase() }.findAll { !it.isEmpty() }
    }

    @Immutable
    private static class NormalizationResult {
        List<String> words
        List<String> unknowns
    }
}
