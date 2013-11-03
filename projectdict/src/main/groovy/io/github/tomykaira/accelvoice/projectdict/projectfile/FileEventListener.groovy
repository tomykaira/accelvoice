package io.github.tomykaira.accelvoice.projectdict.projectfile

import java.nio.file.Path

interface FileEventListener extends EventListener {
    void fileCreated(Path file)

    void fileModified(Path file)

    void fileDeleted(Path file)
}
