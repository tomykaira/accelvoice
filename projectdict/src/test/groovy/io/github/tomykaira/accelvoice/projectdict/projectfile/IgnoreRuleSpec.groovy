package io.github.tomykaira.accelvoice.projectdict.projectfile

import spock.lang.Specification

class IgnoreRuleSpec extends Specification {
    def "rule \"foo/\""() {
        when:
        def rule = new IgnoreRule("foo/")

        then:
        rule.match(path) == result

        where:
        result | path
        true   | "foo/"
        true   | "foo/bar/buz"
        false  | "boo/foo"
        false  | ""
    }

    def "rule \"/foo\""() {
        when:
        def rule = new IgnoreRule("/foo")

        then:
        rule.match(path) == result

        where:
        result | path
        true   | "foo"
        false  | "bar/foo"
        true   | "foo/baz"
        false  | "bar/foo/baz"
    }
}
