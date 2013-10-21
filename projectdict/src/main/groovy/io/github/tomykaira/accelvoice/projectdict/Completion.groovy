package io.github.tomykaira.accelvoice.projectdict

class Completion {
    final Trie trie

    def Completion(Trie trie) {
        this.trie = trie
    }

    def completeByPrefix(String prefix) {
        Trie found = trie.find(prefix)
    }
}
