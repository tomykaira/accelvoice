package io.github.tomykaira.accelvoice.projectdict.projectfile;

import java.io.*;
import java.lang.reflect.Field;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class ProjectWatcher {
    private static final WatchEvent.Kind[] targetKinds =
            new WatchEvent.Kind[]{StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE, StandardWatchEventKinds.ENTRY_DELETE};
    private static final String IGNORE_FILE_NAME = ".accelignore";

    private final Path projectRoot;
    private final FileEventListener listener;
    private WatchService watcher;
    private List<IgnoreRule> ignoreRules = new ArrayList<>();

    public ProjectWatcher(Path projectRoot, FileEventListener listener) {
        this.projectRoot = projectRoot;
        this.listener = listener;
    }

    public void initialize() throws IOException {
        FileSystem fs = projectRoot.getFileSystem();
        watcher = fs.newWatchService();

        File rootFile = projectRoot.toFile();
        File ignoreRuleFile = new File(rootFile, IGNORE_FILE_NAME);
        if (ignoreRuleFile.isFile()) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(new FileInputStream(ignoreRuleFile), Charset.defaultCharset()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    ignoreRules.add(new IgnoreRule(line.trim()));
                }
            }
        }

        watchRecursively(watcher, rootFile);
    }

    public void watch() throws IOException, InterruptedException {
        if (watcher == null) {
            throw new RuntimeException("This ProjectWatcher is not initialized");
        }
        while (true) {
            WatchKey key = watcher.take();
            final Path dir = pickDir(key);
            for (WatchEvent<?> event : key.pollEvents()) {
                Path absolute = dir.resolve((Path)event.context());
                if (isIgnored(absolute))
                    continue;
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
            if (isIgnored(file.toPath()))
                continue;
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

    private boolean isIgnored(Path path) {
        for (IgnoreRule rule : ignoreRules) {
            if (rule.match(projectRoot.relativize(path).toString()))
                return true;
        }
        return false;
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
