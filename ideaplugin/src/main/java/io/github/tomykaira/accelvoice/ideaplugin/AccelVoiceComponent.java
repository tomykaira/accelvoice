package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class AccelVoiceComponent implements ApplicationComponent {
    private static final Logger LOG = Logger.getInstance(AccelVoiceComponent.class.getName());
    private static final RecognizerLibrary library = RecognizerLibrary.INSTANCE;

    public AccelVoiceComponent() {
    }

    public void initComponent() {
        LOG.info("initComponent");

        startRecognizerWithLogFile();

        EditorActionManager manager = EditorActionManager.getInstance();
        TypedAction typedAction = manager.getTypedAction();

        NextInsertionHolder handler = new NextInsertionHolder(typedAction.getHandler());
        typedAction.setupHandler(handler);
        CompletionExecutor.startOnPooledThread(library, handler);
    }

    public void disposeComponent() {
        library.stop();
    }

    @NotNull
    public String getComponentName() {
        return "AccelVoiceComponent";
    }

    public static void startRecognizerWithLogFile() {
        String tempFile = null;
        try {
            tempFile = File.createTempFile("recognizer", ".log").getPath();
        } catch (IOException e) {
            LOG.error("Failed to create log file", tempFile);
        }
        library.start(1, new String[]{"java"}, tempFile);
    }
}
