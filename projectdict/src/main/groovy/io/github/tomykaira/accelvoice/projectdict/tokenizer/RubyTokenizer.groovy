package io.github.tomykaira.accelvoice.projectdict.tokenizer

class RubyTokenizer extends AbstractTokenizer {
    RubyTokenizer() {
        super([".rb"], ["BEGIN", "class", "ensure", "nil", "self", "when", "END", "def",
                "false", "not", "super", "while", "alias", "defined?", "for", "or",
                "then", "yield", "and", "do", "if", "redo", "true", "__LINE__",
                "begin", "else", "in", "rescue", "undef", "__FILE__", "break", "elsif",
                "module", "retry", "unless", "__ENCODING__", "case", "end", "next",
                "return", "until"],
        ['(?m)#.*$', '(?ms)^=begin$.*^=end$', '(?s)__END__.*\\z'])
    }

    @Override
    protected boolean isIdentStart(char c) {
        isAlpha(c) || c == 0x5f
    }

    @Override
    protected boolean isIdentChar(char c) {
        isAlpha(c) || isDigit(c) || c == 0x5f // '_'
    }
}
