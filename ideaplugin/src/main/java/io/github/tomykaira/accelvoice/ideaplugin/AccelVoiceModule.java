package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.actionSystem.EditorActionManager;
import com.intellij.openapi.editor.actionSystem.TypedAction;
import com.intellij.openapi.module.ModuleComponent;
import com.intellij.openapi.module.Module;
import groovy.lang.GroovySystem;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;
import org.jetbrains.annotations.NotNull;

public class AccelVoiceModule implements ModuleComponent {
    private static final Logger LOG = Logger.getInstance(AccelVoiceModule.class.getName());
    private static final RecognizerLibrary library = RecognizerLibrary.INSTANCE;

    public AccelVoiceModule(Module module) {
    }

    public void initComponent() {
        library.start(1, new String[]{"java"});
    }

    public void disposeComponent() {
        library.stop();
    }

    @NotNull
    public String getComponentName() {
        return "AccelVoice Plugin";
    }

    public void projectOpened() {
        // called when project is opened
    }

    public void projectClosed() {
        // called when project is being closed
    }

    public void moduleAdded() {
        System.err.println(System.getProperty("java.class.path"));
        EditorActionManager manager = EditorActionManager.getInstance();
        TypedAction typedAction = manager.getTypedAction();

        NextInsertionHolder handler = new NextInsertionHolder(typedAction.getHandler());
        typedAction.setupHandler(handler);
        CompletionExecutor.startOnPooledThread(library, handler);
    }
}
