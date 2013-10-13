package io.github.tomykaira.accelvoice.selector

/**
 * Load pronouncing dictionary (s.t. cmudict.dic)
 */

class PronouncingDictionary {
    private final Map<String, String> data

    def PronouncingDictionary(Map<String, String> data) {
        this.data = data
    }

    boolean hasWord(String word) {
        if (word.matches(".*\\(\\d+\\)"))
            return false
        return data.containsKey(word)
    }

    static def fromString(String content) {
        def map = new HashMap<String, String>()
        content.split("\n").each { line ->
            def (word, pronunciation) = line.split("\\s+", 2)
            map.put(word, pronunciation)
        }
        new PronouncingDictionary(map)
    }
}
