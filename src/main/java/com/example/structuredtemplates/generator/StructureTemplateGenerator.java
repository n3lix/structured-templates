package com.example.structuredtemplates.generator;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;

import java.nio.charset.StandardCharsets;
import java.util.Properties;

public class StructureTemplateGenerator {

    private final Project project;

    public StructureTemplateGenerator(Project project) {
        this.project = project;
    }

    public void generate(StructureTemplate template, VirtualFile targetDir, String rootName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                // Create root folder
                VirtualFile root = targetDir.findChild(rootName);
                if (root == null) {
                    root = targetDir.createChildDirectory(this, rootName);
                }

                // Generate template contents inside root
                for (StructureEntry entry : template.getEntries()) {
                    createEntry(entry, root, rootName);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private void createEntry(StructureEntry entry, VirtualFile parent, String rootName) {
        try {
            String resolvedName = resolveNamePlaceholders(entry.getName(), rootName);

            if (entry.getType() == StructureEntryType.FOLDER) {
                VirtualFile folder = parent.findChild(resolvedName);
                if (folder == null) {
                    folder = parent.createChildDirectory(this, resolvedName);
                }
                for (StructureEntry child : entry.getChildren()) {
                    createEntry(child, folder, rootName);
                }
            } else {
                FileTemplate template = FileTemplateManager.getInstance(project)
                        .getTemplate(entry.getFileTemplateName());

                if (template == null) {
                    parent.createChildData(this, resolvedName);
                    return;
                }

                Properties props = FileTemplateManager.getInstance(project).getDefaultProperties();
                props.setProperty("FILE_NAME", rootName);
                props.setProperty("FILE_NAME_CAMEL", toCamelCase(rootName));
                props.setProperty("FILE_NAME_PASCAL", toPascalCase(rootName));

                String content = template.getText(props);

                VirtualFile file = parent.findChild(resolvedName);
                if (file == null) {
                    file = parent.createChildData(this, resolvedName);
                }
                file.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String resolveNamePlaceholders(String raw, String rootName) {
        if (raw == null) return null;

        String result = raw;

        if (result.contains("${FILE_NAME_PASCAL}")) {
            result = result.replace("${FILE_NAME_PASCAL}", toPascalCase(rootName));
        }

        if (result.contains("${FILE_NAME_CAMEL}")) {
            result = result.replace("${FILE_NAME_CAMEL}", toCamelCase(rootName));
        }

        if (result.contains("${FILE_NAME}")) {
            result = result.replace("${FILE_NAME}", rootName);
        }

        return result;
    }


    public static String toCamelCase(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        // If already PascalCase (e.g., MyComponent)
        if (input.matches("[A-Z][a-zA-Z0-9]*")) {
            return Character.toLowerCase(input.charAt(0)) + input.substring(1);
        }

        // Otherwise treat as words separated by space, hyphen, underscore
        String[] parts = input.trim().split("[\\s_\\-]+");
        if (parts.length == 0) return "";

        StringBuilder result = new StringBuilder(parts[0].toLowerCase());

        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            if (part.isEmpty()) continue;

            result.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                result.append(part.substring(1).toLowerCase());
            }
        }

        return result.toString();
    }

    public static String toPascalCase(String input) {
        String camel = toCamelCase(input);
        if (camel.isEmpty()) return camel;
        return Character.toUpperCase(camel.charAt(0)) + camel.substring(1);
    }
}
