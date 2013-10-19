package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.codeInsight.completion.CompletionProgressIndicator;
import com.intellij.codeInsight.lookup.LookupElement;

import java.util.List;
import java.util.Objects;

public class CompletionContext {
    public final CompletionProgressIndicator indicator;
    public final List<LookupElement> candidates;

    CompletionContext(CompletionProgressIndicator indicator) {
        this(indicator, indicator.getLookup().getItems());
    }

    CompletionContext(CompletionProgressIndicator indicator, List<LookupElement> candidates) {
        this.indicator = indicator;
        this.candidates = candidates;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final CompletionContext other = (CompletionContext)obj;

        return Objects.equals(indicator, other.indicator) && Objects.equals(candidates, other.candidates);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(this);
    }
}
