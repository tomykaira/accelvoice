package io.github.tomykaira.accelvoice.selector

/**
 * To create corpus
 */

interface NormalizationListener {
    void tokenNormalized(String token, List<String> result)

    void onSureWord(String word)

    void onUnsureWord(String word)
}
