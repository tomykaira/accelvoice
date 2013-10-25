package io.github.tomykaira.accelvoice.projectdict.projectfile;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.*;

public class ProjectWatcher {

    private final Path projectRoot;
    private final FileEventListener listener;
    private static final WatchEvent.Kind[] targetKinds =
            new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE};

    public ProjectWatcher(Path projectRoot, FileEventListener listener) {
        this.projectRoot = projectRoot;
        this.listener = listener;
    }

    public void watch() throws IOException, InterruptedException {
        FileSystem fs = projectRoot.getFileSystem();
        final WatchService watcher = fs.newWatchService();

        watchRecursively(watcher, projectRoot.toFile());

        while (true) {
            WatchKey key = watcher.take();
            final Path dir = pickDir(key);
            for (WatchEvent<?> event : key.pollEvents()) {
                Path absolute = dir.resolve((Path)event.context());
                File file = absolute.toFile();
                WatchEvent.Kind<?> kind = event.kind();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (file.isDirectory()) {
                        watchRecursively(watcher, file);
                    } else if (file.isFile()) {
                        listener.fileCreated(absolute);
                    }
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (file.isFile())
                        listener.fileModified(absolute);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (file.isFile())
                        listener.fileDeleted(absolute);
                }
            }
            key.reset();
        }
    }

    private void watchRecursively(final WatchService watcher, final File root) throws IOException {
        root.toPath().register(watcher, targetKinds);

        for (File file : root.listFiles()) {
            if (file.isDirectory())
                watchRecursively(watcher, file);
            else if (file.isFile())
                listener.fileCreated(file.toPath());
        }
    }

    private static Path pickDir(WatchKey key) {
        try {
            Field dirField = key.getClass().getSuperclass().getDeclaredField("dir");
            dirField.setAccessible(true);
            return (Path)dirField.get(key);
        } catch (NoSuchFieldException|IllegalAccessException e) {
            throw new RuntimeException("Failed to access dir", e);
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        FileEventListener printListener = new FileEventListener() {
            @Override
            public void fileCreated(Path file) {
                System.out.println("File created " + file);
            }

            @Override
            public void fileModified(Path file) {
                System.out.println("File modified " + file);
            }

            @Override
            public void fileDeleted(Path file) {
                System.out.println("File deleted " + file);
            }

        };
        ProjectWatcher watcher = new ProjectWatcher(Paths.get(args[0]), printListener);
        watcher.watch();
    }
}
