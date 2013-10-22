package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.RecognizerLibrary
import io.github.tomykaira.accelvoice.selector.SelectionListener

class Server {
    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Specify project word bag")
            System.exit(1)
        }

        def trie = new File(args[0]).withInputStream { is ->
            new TrieComposer(is).constructTrie()
        }

        def collector = new CandidatesCollector(trie)
        def listener = new SelectionListener() {
            @Override
            void notify(String selected) {
                println(selected)
            }
        }

        RecognizerLibrary.INSTANCE.start(1, {"java"} as String[], null)

        System.in.eachLine { line ->
            def collection = collector.completeByPrefix(line, listener)
            println(collection.candidates)
            collection.select()
        }

        RecognizerLibrary.INSTANCE.stop()
    }
}
