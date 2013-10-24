package io.github.tomykaira.accelvoice.projectdict

import java.nio.file.Path

class Project {
    private final Path projectRoot
    private final Trie trie
    private final ProjectWatcher watcher

    def Project(Path projectRoot) {
        this.projectRoot = projectRoot
        this.trie = new Trie()
        this.watcher = new ProjectWatcher(projectRoot)
    }
}
