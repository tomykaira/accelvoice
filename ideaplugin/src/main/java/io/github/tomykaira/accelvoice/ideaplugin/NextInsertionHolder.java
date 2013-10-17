package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.hint.HintManagerImpl;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.command.impl.CommandProcessorImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import com.intellij.ui.LightweightHint;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class NextInsertionHolder implements TypedActionHandler, VocalCompletionListener {
    private boolean keepTypes = false;
    private final TypedActionHandler originalHandler;

    private final RecognizerLibrary.cb_vader_start startCallback = new RecognizerLibrary.cb_vader_start() {
        @Override
        public void invoke(long timestamp) {
            keepTypes = true;
        }
    };

    private Editor lastEditor;
    private DataContext lastContext;
    private List<Character> pendingSequence = new ArrayList<>();

    public NextInsertionHolder(TypedActionHandler originalHandler) {
        this.originalHandler = originalHandler;
        registerCallbacks();
    }

    private void registerCallbacks() {
        RecognizerLibrary.INSTANCE.register_cb_vader_start(startCallback);
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        if (keepTypes) {
            lastEditor = editor;
            lastContext = dataContext;
            pendingSequence.add(charTyped);
            updatePendingPopup(editor, pendingSequence);
        } else {
            originalHandler.execute(editor, charTyped, dataContext);
        }
    }

    public void flush() {
        if (lastEditor == null || lastContext == null)
            return;
        flush(lastEditor, lastContext);
    }

    private void flush(@NotNull final Editor editor, @NotNull final DataContext dataContext) {
        if (pendingSequence.isEmpty())
            return;
        if (ApplicationManager.getApplication().isWriteAccessAllowed()) {
            doFlush(editor, dataContext);
        } else {
            ApplicationManager.getApplication().invokeLater(new Runnable() {
                @Override
                public void run() {
                    ApplicationManager.getApplication().runWriteAction(new Runnable() {
                        @Override
                        public void run() {
                            CommandProcessor.getInstance().executeCommand(editor.getProject(), new Runnable() {
                                @Override
                                public void run() {
                                    doFlush(editor, dataContext);
                                }
                            }, "FlushStoredStrokes", null);
                        }
                    });
                }
            });
        }
    }

    private void doFlush(@NotNull Editor editor, @NotNull DataContext dataContext) {
        for (Character character : pendingSequence) {
            originalHandler.execute(editor, character, dataContext);
        }
        pendingSequence.clear();
    }

    @Override
    public void completionDone() {
        keepTypes = false;
        flush();
    }

    private void updatePendingPopup(final Editor editor, List<Character> pending) {
        final StringBuilder builder = new StringBuilder();
        for (Character c : pending) {
            builder.append(c);
        }
        final JLabel label = new JLabel(builder.toString());
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                final HintManagerImpl hintManager = HintManagerImpl.getInstanceImpl();
                hintManager.showEditorHint(new LightweightHint(label), editor,
                        HintManager.DEFAULT, 0,
                        0, false);

            }
        });
    }

}
