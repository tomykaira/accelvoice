package io.github.tomykaira.accelvoice.selector

import spock.lang.Specification

class CandidatesCollectionSpec extends Specification {
    class TestRecognizerLibrary implements RecognizerLibrary {
        public boolean started = false, stopped = false;
        public int response = 0;
        public String[][] lastCandidates
        public String[] lastUnknown
        public Object recognized
        boolean failOnStart = false

        @Override
        void start(int argc, String[] argv) {
            started = true
        }

        @Override
        void stop() {
            stopped = true
        }

        @Override
        void register_cb_vader_start(RecognizerLibrary.cb_vader_start cb) {
        }

        @Override
        void register_cb_vader_stop(RecognizerLibrary.cb_vader_stop cb) {
        }

        @Override
        void register_cb_recognized(RecognizerLibrary.cb_recognized cb) {
            recognized = cb
        }

        @Override
        int start_recognition(SplitWordList candidates, String[] unknown) {
            lastCandidates = candidates.getData()
            lastUnknown = unknown
            (recognized as RecognizerLibrary.cb_recognized).invoke(response)
            failOnStart ? -1 : 0
        }

        @Override
        void abort_recognition() {
        }
    }

    def candidates = ["a", "b", "c"]

    def mockLibrary = new TestRecognizerLibrary()

    class TestListener implements SelectionListener {
        public String selected

        @Override
        void notify(String selected) {
            this.selected = selected
        }
    }


    def "candidates collection should return the selected candidate"() {
        when:
        def listener = new TestListener()
        def collection = new CandidatesCollection(candidates, listener)
        collection.setRecognizerLibrary(mockLibrary)
        mockLibrary.response = 1

        then:
        collection.select()
        listener.selected == "b"
        collection.selected == "b"
    }

    def "candidates collection should raise exception if nothing selected"() {
        when:
        def collection = new CandidatesCollection(candidates, null)
        collection.setRecognizerLibrary(mockLibrary)
        mockLibrary.failOnStart = true
        mockLibrary.response = -1
        collection.select()

        then:
        thrown(RecognitionException)
    }
}
