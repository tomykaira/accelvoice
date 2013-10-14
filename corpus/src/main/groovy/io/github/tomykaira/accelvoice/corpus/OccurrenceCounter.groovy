package io.github.tomykaira.accelvoice.corpus

import io.github.tomykaira.accelvoice.selector.NormalizationListener
import io.github.tomykaira.accelvoice.selector.PronouncingDictionary

class OccurrenceCounter implements NormalizationListener {
    private final Map<String, Long> sureWords = new HashMap<>()
    private final Map<String, Long> unsureWords = new HashMap<>()
    static final double MIN_RATIO = 0.000001

    void dump(File output) {
        output.withWriter { writer ->
            writer.println("COUNT\tSURE\t" + sureWords.values().inject(0) { s, b -> s + b })
            writer.println("COUNT\tUNSURE\t" + unsureWords.values().inject(0) { s, b -> s + b })
            sureWords.each { k, v ->
                writer.println("SURE\t$k\t$v")
            }
            unsureWords.each { k, v ->
                writer.println("UNSURE\t$k\t$v")
            }
        }
    }

    void dumpAsDictionary(PronouncingDictionary dictionary, File output) {
        double sum = (sureWords + unsureWords).values().inject(0) { s, b -> s + b }
        output.withWriter { writer ->
            dictionary.pronunciation.sort().each { k, v ->
                writer.println(sprintf("%s\t%.6f\t%s", k, roundedRatio(k, sum), v))
            }
        }
    }

    double roundedRatio(String k, double sum) {
        def found = sureWords.get(k)
        if (k.length() >= 2 && found != null && found > sum * MIN_RATIO) {
            found / sum
        } else {
            MIN_RATIO
        }
    }

    void load(File input) {
        input.withReader { reader ->
            def line
            while ((line = reader.readLine()) != null) {
                def (command, v1, v2) = reader.readLine().split("\t", 3)
                switch (command) {
                case "SURE":
                    sureWords.put(v1, v2 as long)
                    break
                case "UNSURE":
                    unsureWords.put(v1, v2 as long)
                    break
                case "COUNT":
                    // ignored
                    break
                }
            }
        }
    }

    @Override
    void tokenNormalized(String token, List<String> result) {
    }

    @Override
    void onSureWord(String word) {
        sureWords.put(word, (sureWords.get(word)?:0) + 1)
    }

    @Override
    void onUnsureWord(String token) {
        unsureWords.put(token, (unsureWords.get(token)?:0) + 1)
    }
}
