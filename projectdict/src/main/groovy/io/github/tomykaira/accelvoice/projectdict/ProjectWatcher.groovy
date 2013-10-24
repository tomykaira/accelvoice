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

    def ProjectWatcher(String projectRoot) {
        this.projectRoot = Paths.get(projectRoot)
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
                switch (event.kind()) {
                    case StandardWatchEventKinds.ENTRY_CREATE:
                        println("Created " + absolute.toString())
                        if (absolute.toFile().isDirectory()) {
                            watchRecursively(watcher, absolute)
                        }
                        break
                    case StandardWatchEventKinds.ENTRY_MODIFY:
                        println("Modified " + absolute.toString())
                        break
                    case StandardWatchEventKinds.ENTRY_DELETE:
                        println("Deleted " + absolute.toString())
                        break
                }
            }
            key.reset()
        }
    }

    private WatchEvent.Kind[] targetKinds =
        [StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE]

    private void watchRecursively(WatchService watcher, Path root) {
        watchingDirectories.add(root)
        root.register(watcher, targetKinds)

        root.toFile().list().each {
            def path = root.resolve(it)
            if (path.toFile().isDirectory())
                watchRecursively(watcher, path)
        }
    }

    private static Path pickDir(WatchKey key) {
        Field dirField = key.class.superclass.getDeclaredField("dir")
        dirField.setAccessible(true)
        dirField.get(key) as Path
    }

    public static void main(String[] args) {
        ProjectWatcher watcher = new ProjectWatcher(args[0])
        watcher.watch()
    }
}
