package io.github.tomykaira.accelvoice.projectdict

import java.lang.reflect.Field
import java.nio.file.FileSystem
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardWatchEventKinds
import java.nio.file.WatchEvent
import java.nio.file.WatchKey
import java.nio.file.WatchService

class ProjectWatcher {
    final Path projectRoot
    final List<Path> watchingDirectories = []
    private final FileEventListener listener

    def ProjectWatcher(Path projectRoot, FileEventListener listener) {
        this.projectRoot = projectRoot
        this.listener = listener
    }

    def watch() {
        FileSystem fs = projectRoot.getFileSystem()
        WatchService watcher = fs.newWatchService()

        watchRecursively(watcher, projectRoot)

        while (1) {
            WatchKey key = watcher.take()
            Path dir = pickDir(key)
            List<WatchEvent<?>> events = key.pollEvents()
            events.each { event ->
                def absolute = dir.resolve(event.context() as Path)
                File file = absolute.toFile()
                switch (event.kind()) {
                    case StandardWatchEventKinds.ENTRY_CREATE:
                        if (file.isDirectory()) {
                            watchRecursively(watcher, absolute)
                        } else if (file.isFile()) {
                            listener.fileCreated(absolute)
                        }
                        break
                    case StandardWatchEventKinds.ENTRY_MODIFY:
                        if (file.isFile())
                            listener.fileModified(absolute)
                        break
                    case StandardWatchEventKinds.ENTRY_DELETE:
                        if (file.isFile())
                            listener.fileDeleted(absolute)
                        break
                }
            }
            key.reset()
        }
    }

    private static final WatchEvent.Kind[] targetKinds =
        [StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE]

    private void watchRecursively(WatchService watcher, Path root) {
        watchingDirectories.add(root)
        root.register(watcher, targetKinds)

        root.toFile().list().each {
            def path = root.resolve(it)
            def file = path.toFile()
            if (file.isDirectory())
                watchRecursively(watcher, path)
            else if (file.isFile())
                listener.fileCreated(path)
        }
    }

    private static Path pickDir(WatchKey key) {
        Field dirField = key.class.superclass.getDeclaredField("dir")
        dirField.setAccessible(true)
        dirField.get(key) as Path
    }

    public static void main(String[] args) {
        def printListener = new FileEventListener() {
            @Override
            void fileCreated(Path file) {
                println("File created " + file)
            }

            @Override
            void fileModified(Path file) {
                println("File modified " + file)
                //To change body of implemented methods use File | Settings | File Templates.
            }

            @Override
            void fileDeleted(Path file) {
                println("File deleted " + file)
            }
        }
        ProjectWatcher watcher = new ProjectWatcher(Paths.get(args[0]), printListener)
        watcher.watch()
    }
}
