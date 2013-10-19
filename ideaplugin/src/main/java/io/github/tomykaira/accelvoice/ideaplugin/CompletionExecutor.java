package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.codeInsight.completion.CompletionPhase;
import com.intellij.codeInsight.completion.CompletionProgressIndicator;
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl;
import com.intellij.codeInsight.lookup.Lookup;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import io.github.tomykaira.accelvoice.selector.CandidatesCollection;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import io.github.tomykaira.accelvoice.selector.SelectionListener;

import java.util.ArrayList;
import java.util.List;

public class CompletionExecutor implements SelectionListener {
    private static final Logger LOG = Logger.getInstance(CompletionExecutor.class.getName());
    private final RecognizerLibrary library;
    private final VocalCompletionListener listener;
    private CompletionContext currentContext;

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

    public void prepareVocalCompletion(final CompletionProgressIndicator indicator) {
        CompletionContext context = new CompletionContext(indicator);
        if (currentContext == null) {
            LOG.info("Starting recognition");
            currentContext = context;
            startRecognition(context.candidates);
        } else if (!context.equals(currentContext)) {
            stopRecognition();
            prepareVocalCompletion(indicator);
        } // Do nothing if current is the same as given
    }

    private void startRecognition(List<LookupElement> candidates) {
        List<String> lookupStrings = new ArrayList<>();
        for (LookupElement candidate : candidates) {
            lookupStrings.add(candidate.getLookupString());
        }
        CandidatesCollection collection = new CandidatesCollection(lookupStrings, this);
        collection.select();
    }

    @Override
    public void notify(String selected) {
        if (currentContext == null)
            return;
        for (LookupElement candidate : currentContext.candidates) {
            if (candidate.getLookupString().equals(selected)) {
                selectCandidate(candidate);
                return;
            }
        }
    }

    private void selectCandidate(final LookupElement selected) {
        if (currentContext == null || selected == null)
            return;

        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                CommandProcessor.getInstance().executeCommand(currentContext.indicator.getProject(), new Runnable() {
                    @Override
                    public void run() {
                        CompletionProgressIndicator indicator = CompletionServiceImpl.getCompletionPhase().indicator;
                        if (currentContext.indicator != indicator) {
                            return;
                        }
                        indicator.setMergeCommand();
                        indicator.getLookup().finishLookup(Lookup.AUTO_INSERT_SELECT_CHAR, selected);
                    }
                }, "AccelVoice Autocompletion", null);
            }
        });
    }

    public void stopRecognition() {
        if (currentContext != null) {
            LOG.info("Stopping recognition");
            library.abort_recognition();
            currentContext = null;
            listener.completionDone();
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
