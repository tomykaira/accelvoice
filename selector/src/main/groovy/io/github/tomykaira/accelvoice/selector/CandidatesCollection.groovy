package io.github.tomykaira.accelvoice.selector

import groovy.transform.Immutable

class CandidatesCollection {
    final List<String> candidates
    private String selected;
    RecognizerLibrary recognizerLibrary = RecognizerLibrary.INSTANCE
    final Normalizer normalizer = new Normalizer(PronouncingDictionary.fromResource)
    final SelectionListener selectionListener

    def CandidatesCollection(List<String> candidates, SelectionListener selectionListener) {
        this.candidates = candidates
        this.selectionListener = selectionListener
    }

    void setRecognizerLibrary(RecognizerLibrary library) {
        this.recognizerLibrary = library
    }

    void select() {
        if (selected != null)
            return

        def unknowns = new ArrayList<String>()
        def mapping = candidates.collect {
            def result = normalizer.normalize(it)
            unknowns.addAll(result.unknowns)
            new TokenConversionMap(it, result.words)
        }.findAll {
            !it.words.isEmpty()
        }

        recognizerLibrary.register_cb_recognized(CallbackWrapper.callbackRecognized { result ->
            if (result >= 0 && result < mapping.size()) {
                selected = mapping[result].original
                selectionListener.notify(selected)
            }
        })
        dumpQuery(mapping.collect { it.words }, unknowns)
        def result = recognizerLibrary
                .start_recognition(new SplitWordList(mapping.collect { it.words }), unknowns.toArray() as String[])
        if (result < 0)
            throw new RecognitionException()
    }

    private static void dumpQuery(List<List<String>> words, List<String> unknown) {
        def file = File.createTempFile("query", ".log")
        file.withPrintWriter { writer ->
            words.each {
                writer.println(it.join(" "))
            }
            writer.println("")
            unknown.each {
                writer.println(it)
            }
        }
        println("Query is dumped to " + file.toString())
    }

    @Immutable
    private static class TokenConversionMap {
        String original
        List<String> words
    }
}
