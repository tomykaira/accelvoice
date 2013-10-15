package io.github.tomykaira.accelvoice.ideaplugin;

import com.intellij.openapi.actionSystem.DataContext;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.actionSystem.TypedActionHandler;
import org.jetbrains.annotations.NotNull;

public class NextInsertionHolder implements TypedActionHandler, VocalCompletionListener {
    public NextInsertionHolder(TypedActionHandler originalHandler) {
        this.originalHandler = originalHandler;
    }

    @Override
    public void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        originalHandler.execute(editor, charTyped, dataContext);
    }

    private final TypedActionHandler originalHandler;
}
