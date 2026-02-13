package com.example.structuredtemplates.ui.actions;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToolbarAction extends AnAction {

    private Runnable callback;
    private boolean enabled;

    public ToolbarAction(String text, String description, Icon icon) {
        super(text, description, icon);
        this.enabled = true;
    }

    public ToolbarAction onClick(Runnable callback) {
        this.callback = callback;
        return this;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        callback.run();
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        e.getPresentation().setEnabled(enabled);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.EDT;
    }


    public void setEnabled(boolean isEnabled) {
        this.enabled = isEnabled;
    }
}
