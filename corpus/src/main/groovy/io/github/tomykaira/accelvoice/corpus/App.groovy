package io.github.tomykaira.accelvoice.corpus

import io.github.tomykaira.accelvoice.selector.PronouncingDictionary

class App {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.err.println("Project root is not specified")
            System.exit(1)
        }
        def root = new File(args[0])
        def project = new JavaProject(root)
        project.countWordOccurrences()
    }
}
