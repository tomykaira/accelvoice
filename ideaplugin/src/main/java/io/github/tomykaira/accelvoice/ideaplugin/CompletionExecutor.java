package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.codeInsight.completion.CompletionPhase;
import com.intellij.codeInsight.completion.CompletionProgressIndicator;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.impl.LookupImpl;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import io.github.tomykaira.accelvoice.selector.CandidatesCollection;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;

import java.util.ArrayList;
import java.util.List;

public class CompletionExecutor {
    private static final Logger LOG = Logger.getInstance(CompletionExecutor.class.getName());
    private final RecognizerLibrary library;
    private final VocalCompletionListener listener;
    private CompletionProgressIndicator currentIndicator;

    public CompletionExecutor(RecognizerLibrary library, VocalCompletionListener listener) {
        this.library = library;
        this.listener = listener;
    }

    public void waitForItemList() {
        while (true) {
            CompletionPhase phase = CompletionServiceImpl.getCompletionPhase();

            if (phase instanceof CompletionPhase.ItemsCalculated) {
                prepareVocalCompletion(phase.indicator);
            }


            if (CompletionServiceImpl.isPhase(CompletionPhase.NoCompletion.getClass())) {
                stopRecognition();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }

        }

    }

    public void prepareVocalCompletion(CompletionProgressIndicator indicator) {
        if (currentIndicator == null) {
            LOG.info("Starting recognition");
            currentIndicator = indicator;
            LookupElement result = selectCandidate(indicator);
            selectCandidate(result);
        } else if (currentIndicator != indicator) {
            stopRecognition();
            prepareVocalCompletion(indicator);
        } // Do nothing if current is the same as given
    }

    private void selectCandidate(final LookupElement selected) {
        if (currentIndicator == null || selected == null)
            return;

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(currentIndicator.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        CompletionProgressIndicator indicator = CompletionServiceImpl.getCompletionPhase().indicator;
                        if (currentIndicator != indicator) {
                            return;
                        }
                        indicator.setMergeCommand();
                        indicator.getLookup().finishLookup(Lookup.AUTO_INSERT_SELECT_CHAR, selected);
                    }
                }, "AccelVoice Autocompletion", null);
                CompletionServiceImpl.setCompletionPhase(CompletionPhase.NoCompletion);
            }
        });
    }

    private LookupElement selectCandidate(CompletionProgressIndicator indicator) {
        List<LookupElement> candidates = indicator.getLookup().getItems();
        List<String> lookupStrings = new ArrayList<>();
        for (LookupElement candidate : candidates) {
            lookupStrings.add(candidate.getLookupString());
        }
        CandidatesCollection collection = new CandidatesCollection(lookupStrings);
        // TODO: asynchronous call
        String result = collection.select();
        for (LookupElement candidate : candidates) {
            if (candidate.getLookupString().equals(result))
                return candidate;
        }
        return null;
    }

    public void stopRecognition() {
        if (currentIndicator != null) {
            LOG.info("Stopping recognition");
            currentIndicator = null;
        }
    }

    public static void startOnPooledThread(final RecognizerLibrary library, final VocalCompletionListener listener) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                new CompletionExecutor(library, listener).waitForItemList();
            }

        });
    }
}
