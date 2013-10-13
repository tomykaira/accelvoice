package io.github.tomykaira.accelvoice.selector

class NormalizationFailedException extends RuntimeException {
    String token

    def NormalizationFailedException(String token) {
        super("Failed to normalize " + token)
        this.token = token
    }
}
