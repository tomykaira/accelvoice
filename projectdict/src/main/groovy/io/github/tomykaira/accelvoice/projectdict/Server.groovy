package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.selector.RecognizerLibrary
import io.github.tomykaira.accelvoice.selector.SelectionListener

class Server {
    private final Trie trie
    private final File logFile

    def Server(File dictionary, File logFile) {
        trie = dictionary.withInputStream { is ->
            new TrieComposer(is).constructTrie()
        } as Trie
        this.logFile = logFile
    }

    def run() {
        def collector = new CandidatesCollector(trie)
        def listener = new SelectionListener() {
            @Override
            void notify(String selected) {
                log("Result: " + selected)
                println(selected)
            }
        }

        RecognizerLibrary.INSTANCE.start(1, {"java"} as String[], logFile?.toString())

        System.in.eachLine { line ->
            def collection = collector.completeByPrefix(line, listener)
            log(collection.candidates.join(" "))
            collection.select()
        }

        RecognizerLibrary.INSTANCE.stop()
    }

    private def log(String content) {
        def data = "INFO: Server: " + content
        if (logFile == null) {
            println(data)
        } else {
            logFile.withWriterAppend { w ->
                w.write(data + "\n")
            }
        }
    }

    public static void main(String[] args) {
        if (args.length < 1) {
            System.err.println("Specify project word bag")
            System.exit(1)
        }
        def logFile = args.length >= 2 ? new File(args[1]) : null

        def server = new Server(new File(args[0]), logFile)
        server.run()
    }
}
