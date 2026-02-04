package com.example.structuredtemplates;

import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;

import java.util.List;

public class CreateFromStructureTemplateGroup extends DefaultActionGroup {

    public CreateFromStructureTemplateGroup() {
        super("Create From Structure Template", true); // popup = true
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);

        removeAll(); // refresh menu items dynamically

        Project project = e.getProject();
        if (project == null) return;

        List<StructureTemplate> templates =
                TemplateSettings.getInstance(project).getTemplates();

        for (StructureTemplate template : templates) {
            add(new CreateFromStructureTemplateAction(template.getName()));
        }
    }
}
