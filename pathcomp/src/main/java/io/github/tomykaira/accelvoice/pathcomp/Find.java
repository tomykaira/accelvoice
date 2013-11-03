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

    public CandidatesCollection findCandidates(SelectionListener listener) throws IOException {
        List<String> candidates = new ArrayList<>();
        findRecursively(root.toFile(), candidates);
        return new CandidatesCollection(candidates, listener);
    }

    private void findRecursively(final File parent, final List<String> paths) throws IOException {
        for (File file : parent.listFiles()) {
            if (isIgnored(file))
                continue;
            paths.add(file.toPath().relativize(root).toString());
            if (file.isDirectory())
                findRecursively(file, paths);
        }
    }

    private boolean isIgnored(File file) {
        return false;
    }
}
