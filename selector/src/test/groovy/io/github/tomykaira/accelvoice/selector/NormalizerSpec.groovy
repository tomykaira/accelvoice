package io.github.tomykaira.accelvoice.selector

import spock.lang.Specification

class NormalizerSpec extends Specification {
    def data = "BACK                 B AE K\n" +
            "LOG                  L AO G"
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
    }
}
