package io.github.tomykaira.accelvoice.selector

/**
 * Load pronouncing dictionary (s.t. cmudict.dic)
 */

class PronouncingDictionary {
    final Map<String, String> data

    def PronouncingDictionary(Map<String, String> data) {
        this.data = data
    }

    boolean hasWord(String word) {
        if (word.matches(".*\\(\\d+\\)"))
            return false
        return data.containsKey(word)
    }

    private static PronouncingDictionary fromLines(List<String> lines) {
        def map = new HashMap<String, String>()
        lines.each { line ->
            def (word, pronunciation) = line.split("\\s+", 2)
            map.put(word, pronunciation)
        }
        new PronouncingDictionary(map)
    }

    static PronouncingDictionary fromString(String content) {
        fromLines(content.split("\n") as List<String>)
    }

    static PronouncingDictionary fromResource() {
        this.class.getResource("/cmudict_keywords.dic").withReader { reader ->
            fromLines(reader.readLines())
        }
    }
}
