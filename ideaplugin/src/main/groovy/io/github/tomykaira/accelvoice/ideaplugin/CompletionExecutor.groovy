package io.github.tomykaira.accelvoice.ideaplugin

import com.intellij.codeInsight.completion.CompletionPhase
import com.intellij.codeInsight.completion.CompletionProgressIndicator
import com.intellij.codeInsight.completion.impl.CompletionServiceImpl
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.Logger
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary

class CompletionExecutor {
    private static final Logger LOG = Logger.getInstance(CompletionExecutor.class.getName());

    private final RecognizerLibrary library
    private final VocalCompletionListener listener

    def CompletionExecutor(RecognizerLibrary library, VocalCompletionListener listener) {
        this.library = library
        this.listener = listener
    }

    void waitForItemList() {
        while (true) {
            CompletionPhase phase = CompletionServiceImpl.getCompletionPhase();

            if (phase instanceof CompletionPhase.ItemsCalculated) {
                LOG.info("Completion items are ready");
                prepareVocalCompletion(phase.indicator)
            }

            if (CompletionServiceImpl.isPhase(CompletionPhase.NoCompletion.getClass())) {
                LOG.info("Completion list is closed");
                stopRecognition();
            }

            try {
                Thread.sleep(100);
            } catch (InterruptedException ignored) {
            }
        }
    }

    void prepareVocalCompletion(CompletionProgressIndicator completionProgressIndicator) {

    }

    void stopRecognition() {

    }

    static void startOnPooledThread(final RecognizerLibrary library, final VocalCompletionListener listener) {
        ApplicationManager.getApplication().executeOnPooledThread(new Runnable() {
            @Override
            public void run() {
                new CompletionExecutor(library, listener).waitForItemList();
            }
        });
    }
}
