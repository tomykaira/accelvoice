package io.github.tomykaira.accelvoice.selector

import groovy.transform.Immutable

class CandidatesCollection {
    final List<String> candidates
    private String selected;
    RecognizerLibrary recognizerLibrary = RecognizerLibrary.INSTANCE
    List<CandidatesCollection.TokenConversionMap> mapping
    Normalizer normalizer = new Normalizer(PronouncingDictionary.fromResource)

    def CandidatesCollection(List<String> candidates) {
        this.candidates = candidates
    }

    void setRecognizerLibrary(RecognizerLibrary library) {
        this.recognizerLibrary = library
    }

    String select() {
        if (selected != null)
            return selected

        def unknowns = []
        mapping = candidates.collect {
            def result = normalizer.normalize(it)
            unknowns.addAll(result.unknowns)
            new TokenConversionMap(it, result.words)
        }
        def result = recognizerLibrary
                .recognize(mapping.collect { it.words.toArray() }.toArray() as String[][], [] as String[])
        if (result < 0 || result >= mapping.size())
            throw new RecognitionException()
        selected = mapping[result].original
    }

    @Immutable
    private static class TokenConversionMap {
        String original
        List<String> words
    }
}
