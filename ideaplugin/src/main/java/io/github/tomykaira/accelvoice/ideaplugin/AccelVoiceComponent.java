package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import org.jetbrains.annotations.NotNull;

public class AccelVoiceComponent implements ApplicationComponent {
    private static final Logger LOG = Logger.getInstance(AccelVoiceComponent.class.getName());
    private static final RecognizerLibrary library = RecognizerLibrary.INSTANCE;

    public AccelVoiceComponent() {
    }

    public void initComponent() {
        LOG.info("initComponent");

        library.start(1, new String[]{"java"});

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
}
