package io.github.tomykaira.accelvoice.projectdict

import java.nio.file.Path

interface FileEventListener extends EventListener {
    def fileCreated(Path file)

    def fileModified(Path file)

    def fileDeleted(Path file)
}
