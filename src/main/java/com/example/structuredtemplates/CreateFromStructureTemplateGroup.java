package com.example.structuredtemplates;

import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import org.jetbrains.annotations.NotNull;
import com.intellij.openapi.project.Project;
import com.intellij.icons.AllIcons;
import javax.swing.*;
import java.lang.reflect.Field;

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
            add(new CreateFromStructureTemplateAction(template.getName(), getIconByPath(template.getIconPath())));
        }
    }

    private Icon getIconByPath(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("AllIcons.")) {
            String[] parts = path.split("\\.");
            if (parts.length == 3) {
                try {
                    Class<?> clazz = null;
                    if (parts[1].equals("Nodes")) clazz = AllIcons.Nodes.class;
                    else if (parts[1].equals("Actions")) clazz = AllIcons.Actions.class;

                    if (clazz != null) {
                        Field field = clazz.getDeclaredField(parts[2]);
                        return (Icon) field.get(null);
                    }
                } catch (Exception e) {
                    return null;
                }
            }
        }
        return null;
    }
}
