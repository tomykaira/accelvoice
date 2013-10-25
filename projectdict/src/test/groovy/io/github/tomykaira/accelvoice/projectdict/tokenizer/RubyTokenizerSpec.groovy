package io.github.tomykaira.accelvoice.projectdict.tokenizer

import spock.lang.Specification

import java.nio.file.Paths

class RubyTokenizerSpec extends Specification {
    def "tokenize an arithmetic line"() {
        when:
        def tokenizer = new RubyTokenizer()
        tokenizer.tokenize("_width * _width * 3 == height3")

        then:
        tokenizer.tokens["_width"] == 2
        tokenizer.tokens["height3"] == 1
        tokenizer.tokens["3"] == null
    }

    def "ignore keywords"() {
        when:
        def tokenizer = new RubyTokenizer()
        tokenizer.tokenize("class Foo; def method; end; end")

        then:
        tokenizer.tokens[token] == result

        where:
        token      | result
        "Foo"      | 1
        "method"   | 1
        "class"    | null
        "def"      | null
        "end"      | null
        ";"        | null
    }

    def "ignore line comments"() {
        when:
        def code = """\
test.call # this is test
test.call # test again
=begin
should not counted
test test test
=end
test.last_call
__END__
test test
"""
        def tokenizer = new RubyTokenizer()
        tokenizer.tokenize(code)

        then:
        tokenizer.tokens[token] == result

        where:
        token      | result
        "test"     | 3
        "call"     | 2
        "this"     | null
    }

    def ".matchExtension()"() {
        when:
        def tokenizer = new RubyTokenizer()

        then:
        tokenizer.matchExtension(Paths.get("/tmp/test.rb"))
        !tokenizer.matchExtension(Paths.get("/tmp/test.rb.gz"))
        !tokenizer.matchExtension(Paths.get("/tmp/test.erb"))
    }
}
