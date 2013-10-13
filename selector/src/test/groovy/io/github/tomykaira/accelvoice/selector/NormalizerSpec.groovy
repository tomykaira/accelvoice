package io.github.tomykaira.accelvoice.selector

import spock.lang.Specification

class NormalizerSpec extends Specification {
    def data = "BACK\t0.005\tB AE K\n" +
            "LOG\t0.005\tL AO G"
    def dictionary = PronouncingDictionary.fromString(data)
    def fullDictionary = PronouncingDictionary.fromResource()

    def "it should return the word if the dictionary contains"() {
        when:
        def normalizer = new Normalizer(dictionary)

        then:
        normalizer.normalize(token) == list

        where:
        token        | list
        "BACK"       | ["BACK"]
        "LOG"        | ["LOG"]
    }

    def "it should normalize case and unpronounceable chars"() {
        when:
        def normalizer = new Normalizer(dictionary)

        then:
        normalizer.normalize(token) == list

        where:
        token        | list
        "back"       | ["BACK"]
        "Back"       | ["BACK"]
        "_back"      | ["BACK"]
    }

    def "it should tokenize composite words"() {
        when:
        def normalizer = new Normalizer(dictionary)

        then:
        normalizer.normalize(token) == list

        where:
        token        | list
        "back_log"   | ["BACK", "LOG"]
        "BACK_LOG"   | ["BACK", "LOG"]
        "backLog"    | ["BACK", "LOG"]
        "backlog"    | ["BACK", "LOG"]
    }

    def "it should raise NormalizationFailed exception"() {
        when:
        def normalizer = new Normalizer(dictionary)
        normalizer.normalize("unknown")

        then:
        thrown(NormalizationFailedException)
    }

    def "it should notify incidents to the Listener"() {
        when:
        final def results = new ArrayList<List<String>>()
        final def unsure = new ArrayList<String>()
        final def sure = new ArrayList<String>()
        def listener = new NormalizationListener() {
            @Override
            void tokenNormalized(String token, List<String> result) {
                results.add(result)
            }

            @Override
            void onSureWord(String token, String word) {
                sure.add(word)
            }

            @Override
            void onUnsureWords(String token, List<String> estimate) {
                unsure.add(token)
            }
        }
        def normalizer = new Normalizer(dictionary, listener)
        normalizer.normalize("BACK")
        normalizer.normalize("BACK_LOG")
        normalizer.normalize("BACKLOG")

        then:
        results == [["BACK"], ["BACK", "LOG"], ["BACK", "LOG"]]
        unsure == ["BACKLOG"]
        sure == ["BACK", "BACK", "LOG"]
    }

    def "it should process unknown words with the full dictionary"() {
        when:
        def normalizer = new Normalizer(fullDictionary)

        then:
        normalizer.normalize(token) == list

        where:
        token                   | list
        "resourcenameOther"     | ["RESOURCE", "NAME", "OTHER"]
        "addRfsRule"            | ["ADD", "R", "FS", "RULE"]  // TODO: Split f and s
        "FILEPATH"              | ["FILE", "PATH"]
        "INSERTROW"             | ["INSERT", "ROW"]
        "setScaleRendermode"    | ["SET", "SCALE", "RENDER", "MODE"]
        "JSONException"         | ["J", "SON", "EXCEPTION"]   // questionable
        "initActions"           | ["INIT", "ACTIONS"]
        "Subfolders"            | ["SUB", "FOLDERS"]
        "time1"                 | ["TIME", "ONE"]
        "as400"                 | ["AS", "FOUR", "ZERO", "ZERO"] // TODO
    }

    def "it should select a combination with the highest possibility"() {
        when:
        def normalizer = new Normalizer(fullDictionary)

        then:
        normalizer.normalize(token) == list

        where:
        token                   | list
        "ADDSOURCE"             | ["ADD", "SOURCE"]
        "ALLINFO"               | ["ALL", "INFO"]
        "CDATA"                 | ["C", "DATA"]
    }
}
