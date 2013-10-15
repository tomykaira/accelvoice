package io.github.tomykaira.accelvoice.ideaplugin

import com.intellij.openapi.actionSystem.DataContext
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.actionSystem.TypedActionHandler
import org.jetbrains.annotations.NotNull

class NextInsertionHolder implements TypedActionHandler, VocalCompletionListener {
    private final TypedActionHandler originalHandler

    def NextInsertionHolder(TypedActionHandler originalHandler) {
        this.originalHandler = originalHandler
    }

    @Override
    void execute(@NotNull Editor editor, char charTyped, @NotNull DataContext dataContext) {
        originalHandler.execute(editor, charTyped, dataContext);
    }
}
