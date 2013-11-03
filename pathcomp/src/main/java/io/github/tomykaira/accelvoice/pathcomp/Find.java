package io.github.tomykaira.accelvoice.pathcomp;

import io.github.tomykaira.accelvoice.selector.CandidatesCollection;
import io.github.tomykaira.accelvoice.selector.SelectionListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Search files recursively like UNIX find command.
 */
public class Find {
    private final Path root;

    public Find(Path root) {
        this.root = root;
    }

    public CandidatesCollection findCandidates(SelectionListener listener, int depth) throws IOException {
        List<String> candidates = new ArrayList<>();
        findRecursively(root.toFile(), candidates, depth);
        return new CandidatesCollection(candidates, listener);
    }

    private void findRecursively(final File parent, final List<String> paths, int depth) throws IOException {
        for (File file : parent.listFiles()) {
            if (isIgnored(file))
                continue;
            String relativePath = root.relativize(file.toPath()).toString();
            paths.add(relativePath);
            if (file.isDirectory() && depth > 0)
                findRecursively(file, paths, depth-1);
        }
    }

    private boolean isIgnored(File file) {
        return file.getName().equals(".git");
    }
}
