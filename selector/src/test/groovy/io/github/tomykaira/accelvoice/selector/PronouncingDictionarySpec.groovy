package io.github.tomykaira.accelvoice.selector

import spock.lang.Specification

class PronouncingDictionarySpec extends Specification {
    def "from string"() {
        when:
        def content = "HELLNER              HH EH L N ER\n" +
                "HELLO                HH AH L OW\n" +
                "HELLO(2)             HH EH L OW\n" +
                "HELLRAISER           HH EH L R EY Z ER"
        PronouncingDictionary dict = PronouncingDictionary.fromString(content)

        then:
        dict.hasWord(word) == included

        where:
        word         | included
        "HELLNER"    | true
        "HELLO"      | true
        "HELLO(2)"   | false
        "HELLRAISER" | true
        "HAPPY"      | false
    }

    def "from resource"() {
        when:
        def dict = PronouncingDictionary.fromResource()

        then:
        dict.hasWord("HELLO") == true
    }
}
