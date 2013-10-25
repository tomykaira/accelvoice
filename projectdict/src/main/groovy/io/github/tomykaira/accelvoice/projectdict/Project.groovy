package io.github.tomykaira.accelvoice.projectdict

import io.github.tomykaira.accelvoice.projectdict.projectfile.FileEventListener
import io.github.tomykaira.accelvoice.projectdict.projectfile.ProjectWatcher
import io.github.tomykaira.accelvoice.projectdict.tokenizer.RubyTokenizer

import java.nio.file.Path
import java.nio.file.Paths

class Project implements FileEventListener {
    private final Path projectRoot
    final Trie trie
    private final ProjectWatcher watcher

    def Project(String projectRoot) {
        this(Paths.get(projectRoot))
    }

    def Project(Path projectRoot) {
        this.projectRoot = projectRoot
        this.trie = new Trie()
        this.watcher = new ProjectWatcher(projectRoot, this)
    }

    def startWatching() {
        Thread.start {
            watcher.watch()
        }
    }

    @Override
    void fileCreated(Path file) {
        addIdentitiesInFile(file)
    }

    @Override
    void fileModified(Path file) {
        addIdentitiesInFile(file)
    }

    private void addIdentitiesInFile(Path file) {
        RubyTokenizer tokenizer = new RubyTokenizer()
        if (!tokenizer.matchExtension(file)) {
            return
        }
        tokenizer.tokenize(file.toFile().readLines().join("\n"))
        tokenizer.tokens.each { pair ->
            trie.insert(pair.key, pair.value)
        }
    }

    @Override
    void fileDeleted(Path file) {
    }
}
