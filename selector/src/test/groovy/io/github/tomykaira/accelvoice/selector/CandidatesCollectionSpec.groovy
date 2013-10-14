package io.github.tomykaira.accelvoice.selector

import spock.lang.Specification

class CandidatesCollectionSpec extends Specification {
    class TestRecognizerLibrary implements RecognizerLibrary {
        public boolean started = false, stopped = false;
        public int response = 0;
        public String[][] lastCandidates
        public String[] lastUnknown

        @Override
        void start(int argc, String[] argv) {
            started = true
        }

        @Override
        void stop() {
            stopped = true
        }

        @Override
        int recognize(String[][] candidates, String[] unknown) {
            lastCandidates = candidates
            lastUnknown = unknown
            return response
        }
    }

    def candidates = ["a", "b", "c"]

    def mockLibrary = new TestRecognizerLibrary()

    def "candidates collection should return the selected candidate"() {
        when:
        def collection = new CandidatesCollection(candidates)
        collection.setRecognizerLibrary(mockLibrary)
        mockLibrary.response = 1

        then:
        collection.select() == "b"
    }

    def "candidates collection should raise exception if nothing selected"() {
        when:
        def collection = new CandidatesCollection(candidates)
        collection.setRecognizerLibrary(mockLibrary)
        mockLibrary.response = -1
        collection.select() == "b"

        then:
        thrown(RecognitionException)
    }
}
