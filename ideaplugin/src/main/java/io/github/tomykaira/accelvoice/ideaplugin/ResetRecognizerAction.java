package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.PlatformDataKeys;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import io.github.tomykaira.accelvoice.selector.RecognizerLibrary;

public class ResetRecognizerAction extends AnAction {
    public ResetRecognizerAction() {
        super("_Reset Recognizer");
    }

    public void actionPerformed(AnActionEvent event) {
        RecognizerLibrary library = RecognizerLibrary.INSTANCE;
        library.stop();
        AccelVoiceComponent.startRecognizerWithLogFile();
    }
}
