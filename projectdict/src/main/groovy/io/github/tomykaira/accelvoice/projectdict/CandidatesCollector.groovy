package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.CandidatesCollection
import io.github.tomykaira.accelvoice.selector.SelectionListener

class CandidatesCollector {
    final Trie trie

    def CandidatesCollector(Trie trie) {
        this.trie = trie
    }

    CandidatesCollection completeByPrefix(String prefix, SelectionListener listener) {
        Trie found = trie.find(prefix)
        new CandidatesCollection(found.toWordList().findAll { !it.isEmpty() }, listener)
    }
}
