package io.github.tomykaira.accelvoice.selector

/**
 * Load pronouncing dictionary (s.t. cmudict.dic)
 */

class PronouncingDictionary {
    final Map<String, String> pronunciation
    final Map<String, Double> ratio

    def PronouncingDictionary(Map<String, String> pronunciation, Map<String, Double> ratio) {
        this.pronunciation = pronunciation
        this.ratio = ratio
    }

    boolean hasWord(String word) {
        if (word.matches(".*\\(\\d+\\)"))
            return false
        return pronunciation.containsKey(word)
    }

    double ratioOf(String word) {
        ratio.get(word)?:0
    }

    private static PronouncingDictionary fromLines(List<String> lines) {
        def pron = new HashMap<String, String>()
        def ratio = new HashMap<String, Double>()
        lines.each { line ->
            def (word, weight, pronunciation) = line.split("\\s+", 3)
            pron.put(word, pronunciation)
            ratio.put(word, weight as double)
        }
        new PronouncingDictionary(pron, ratio)
    }

    static PronouncingDictionary fromString(String content) {
        fromLines(content.split("\n") as List<String>)
    }

    static PronouncingDictionary fromResource() {
        this.class.getResource("/java_cmudict.dic").withReader { reader ->
            fromLines(reader.readLines())
        }
    }
}
