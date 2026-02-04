package com.example.dynamicstructure;

import com.example.dynamicstructure.model.StructureTemplate;
import com.example.dynamicstructure.settings.TemplateSettings;
import com.example.dynamicstructure.generator.StructureTemplateGenerator;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

public class CreateFromStructureTemplateAction extends AnAction {

    private final String templateName;

    public CreateFromStructureTemplateAction(String templateName) {
        super(templateName);
        this.templateName = templateName;
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
                "Enter name for the new component:",
                "Component Name",
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
