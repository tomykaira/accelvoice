package io.github.tomykaira.accelvoice.selector

class RecognitionException extends RuntimeException {
    def RecognitionException() {
        super("Failed to recognize vocal input")
    }
}
