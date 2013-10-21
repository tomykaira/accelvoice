package io.github.tomykaira.accelvoice.projectdict

class TrieComposer {
    final InputStream is

    def TrieComposer(InputStream is) {
        this.is = is
    }

    Trie constructTrie() {
        def root = new Trie()
        is.eachLine { line ->
            def (word, count) = line.split("\t", 2)
            root.insert(word, Integer.parseInt(count))
        }
        root
    }
}
