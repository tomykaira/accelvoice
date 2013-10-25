package io.github.tomykaira.accelvoice.projectdict

import spock.lang.Specification

class TrieSpec extends Specification {
    private static def compose(String bag) {
        def is = new ByteArrayInputStream(bag.getBytes("UTF-8"))
        new TrieComposer(is).constructTrie()
    }

    def "compose Trie tree from a word"() {
        when:
        def trie = compose("hello\t3")

        then:
        trie.count == 3
        trie.children["hello"].count == 3
    }

    def "compose Trie tree from hello and help"() {
        when:
        def trie = compose("hello\t3\nhelp\t2")

        then:
        trie.count == 5
        def sub = trie.children["hel"]
        sub.children["lo"].count == 3
        sub.children["p"].count == 2
    }

    def "compose Trie tree from hello, help, heli"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nheli\t1")

        then:
        trie.count == 6
        def sub = trie.children["hel"]
        sub.children["lo"].count == 3
        sub.children["p"].count == 2
        sub.children["i"].count == 1
    }

    def "compose Trie tree from hello, help, hell"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        trie.count == 6
        def sub = trie.children["hel"]
        sub.children["l"].count == 4
        sub.children["l"].children[""].count == 1
        sub.children["l"].children["o"].count == 3
        sub.children["p"].count == 2
    }

    def "compose Trie tree from gone, gogo"() {
        when:
        def trie = compose("gone\t3\ngogo\t1")

        then:
        def sub = trie.children["go"]
        sub.children["ne"].count == 3
        sub.children["go"].count == 1
    }

    def "compose Trie tree from est, eval, e"() {
        when:
        def trie = compose("est\t3\neval\t1\ne\t1")

        then:
        def sub = trie.children["e"]
        sub.children["st"].count == 3
        sub.children["val"].count == 1
        sub.children[""].count == 1
    }

    def "insert the same word twice"() {
        when:
        def trie = new Trie()
        trie.insert("foo", 1)
        trie.insert("fuss", 2)
        trie.insert("foo", 2)

        then:
        trie.children["f"].children["oo"].count == 3
        trie.children["f"].children["oo"].children[""].count == 3
        trie.children["f"].children["uss"].count == 2
    }

    def "find with prefix he"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        trie.find("he") == trie
    }

    def "find with prefix hell"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        def result = trie.find("hell")
        result.children["hell"].children["o"].count == 3
        result.children["hell"].children[""].count == 1
    }

    def "find with prefix heli"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        trie.find("heli") == new Trie()
    }

    def "find with prefix eve from every and everybody"() {
        when:
        def trie = compose("every\t1\neverybody\t1\n")
        def found = trie.find("eve")

        then:
        found.toWordList() == ["every", "everybody"]
    }

    def "find with empty string"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        trie.find("") == trie
    }

    def "word list"() {
        when:
        def trie = compose("hello\t3\nhelp\t2\nhell\t1")

        then:
        trie.toWordList() == ["help", "hello", "hell"]
    }
}
