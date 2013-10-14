package io.github.tomykaira.accelvoice.corpus

import groovy.io.FileType
import io.github.tomykaira.accelvoice.selector.NormalizationFailedException
import io.github.tomykaira.accelvoice.selector.Normalizer
import io.github.tomykaira.accelvoice.selector.PronouncingDictionary

class JavaProject {
    final File projectRoot
    final TokenCollection tokens
    private boolean traversed = false

    def JavaProject(File projectRoot) {
        this.projectRoot = projectRoot
        this.tokens = new TokenCollection()
    }

    List<String> reportAbnormalTokens() {
        traverse()
        def failed = []
        def normalizer = new Normalizer(PronouncingDictionary.fromResource())
        tokens.occurrence.keySet().each { token ->
            try {
                normalizer.normalize(token)
            } catch (NormalizationFailedException e) {
                failed.add(e.token)
            }
        }
        failed
    }

    OccurrenceCounter countWordOccurrences() {
        traverse()
        def counter = new OccurrenceCounter()
        def normalizer = new Normalizer(PronouncingDictionary.fromResource(), counter)
        tokens.occurrence.keySet().each { token ->
            normalizer.normalize(token)
        }
        counter
    }

    void traverse() {
        if (traversed)
            return
        projectRoot.traverse(type: FileType.FILES, nameFilter: ~/.*\.java$/) { file ->
            tokens.tokenize(file)
        }
        traversed = true
    }
}
