package com.example.structuredtemplates;

import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.example.structuredtemplates.generator.StructureTemplateGenerator;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CreateFromStructureTemplateAction extends AnAction {

    private final String templateName;
    private final Icon icon;

    public CreateFromStructureTemplateAction(String templateName, Icon icon) {
        super(templateName, null, icon);
        this.templateName = templateName;
        this.icon = icon;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Project project = e.getProject();
        if (project == null) return;

        VirtualFile targetDir = getTargetDirectory(e);
        if (targetDir == null) {
            Messages.showErrorDialog(project, "No directory selected.", "Error");
            return;
        }

        StructureTemplate template = TemplateSettings.getInstance(project)
                .getTemplates()
                .stream()
                .filter(t -> t.getName().equals(templateName))
                .findFirst()
                .orElse(null);

        if (template == null) {
            Messages.showErrorDialog(project, "Template not found.", "Error");
            return;
        }

        String name = Messages.showInputDialog(
                project,
                "Enter name for the new " + templateName + ":",
                templateName + " Name",
                Messages.getQuestionIcon()
        );

        if (name == null || name.trim().isEmpty()) {
            return;
        }

        new StructureTemplateGenerator(project)
                .generate(template, targetDir, name.trim());
    }

    private VirtualFile getTargetDirectory(AnActionEvent e) {
        VirtualFile vf = e.getData(CommonDataKeys.VIRTUAL_FILE);
        if (vf != null && vf.isDirectory()) return vf;
        return null;
    }
}
