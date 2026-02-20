package com.example.structuredtemplates.generator;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.codeInsight.template.impl.TextExpression;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.OpenFileDescriptor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StructureTemplateGenerator {

    private final Project project;

    public StructureTemplateGenerator(Project project) {
        this.project = project;
    }

    public void generate(StructureTemplate template, VirtualFile targetDir, String rootName) {
        WriteCommandAction.runWriteCommandAction(project, () -> {
            try {
                VirtualFile root = targetDir.findChild(rootName);
                if (root == null) {
                    root = targetDir.createChildDirectory(this, rootName);
                }

                for (StructureEntry entry : template.getEntries()) {
                    createEntry(entry, root, rootName);
                }

            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });
    }

    private FileTemplate findTemplateByName(String name) {
        FileTemplateManager manager = FileTemplateManager.getInstance(project);
        FileTemplate[] templates = manager.getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);

        for (FileTemplate t : templates) {
            if (t.getName().equals(name)) {
                return t;
            }
        }
        return null;
    }


    private void createEntry(StructureEntry entry, VirtualFile parent, String rootName) {
        try {
            String resolvedName = resolveNamePlaceholders(entry.getName(), rootName, entry);

            if (entry.getType() == StructureEntryType.FOLDER) {
                VirtualFile folder = parent.findChild(resolvedName);
                if (folder == null) {
                    folder = parent.createChildDirectory(this, resolvedName);
                }
                for (StructureEntry child : entry.getChildren()) {
                    createEntry(child, folder, rootName);
                }
            } else {
                FileTemplate template = findTemplateByName(entry.getFileTemplateName());

                if (template == null) {
                    parent.createChildData(this, resolvedName);
                    return;
                }

                Properties props = FileTemplateManager.getInstance(project).getDefaultProperties();
                props.setProperty("FILE_NAME", rootName);
                props.setProperty("FILE_NAME_CAMEL", toCamelCase(rootName));
                props.setProperty("FILE_NAME_PASCAL", toPascalCase(rootName));
                props.setProperty("FILE_NAME_KEBAB", toKebabCase(rootName));

                // Add custom variables
                entry.getCustomVariables().forEach(props::setProperty);

                String content = template.getText(props);

                VirtualFile file = parent.findChild(resolvedName);
                if (file == null) {
                    String ext = template.getExtension().isEmpty() ? "" : "." + template.getExtension();
                    String fullName = resolvedName.endsWith(ext) ? resolvedName : resolvedName + ext; // ignore extension if the filename already has it
                    file = parent.createChildData(this, fullName);
                }

                file.setBinaryContent(content.getBytes(StandardCharsets.UTF_8));
                Pattern pattern = Pattern.compile("\\$([A-Za-z0-9_]+)\\$");

                boolean hasVariables = pattern.matcher(content).find();
                if (!hasVariables) {
                    return;
                }

                PsiFile psiFile = PsiManager.getInstance(project).findFile(file);
                Editor editor = FileEditorManager.getInstance(project)
                        .openTextEditor(new OpenFileDescriptor(project, file), true);

                TemplateBuilderImpl builder = new TemplateBuilderImpl(psiFile);

                String text = psiFile.getText();
                Matcher matcher = pattern.matcher(text);

                while (matcher.find()) {
                    String varName = matcher.group(1);

                    PsiElement element = psiFile.findElementAt(matcher.start());
                    if (element != null) {
                        builder.replaceElement(element, varName, new TextExpression(varName), true);
                    }
                }

                Template tmpl = builder.buildInlineTemplate();
                TemplateManager.getInstance(project).startTemplate(editor, tmpl);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private String resolveNamePlaceholders(String raw, String rootName, StructureEntry entry) {
        if (raw == null) return null;

        String result = raw;

        if (result.contains("${FILE_NAME_PASCAL}")) {
            result = result.replace("${FILE_NAME_PASCAL}", toPascalCase(rootName));
        }

        if (result.contains("${FILE_NAME_CAMEL}")) {
            result = result.replace("${FILE_NAME_CAMEL}", toCamelCase(rootName));
        }

        if (result.contains("${FILE_NAME_KEBAB}")) {
            result = result.replace("${FILE_NAME_KEBAB}", toKebabCase(rootName));
        }

        if (result.contains("${FILE_NAME}")) {
            result = result.replace("${FILE_NAME}", rootName);
        }

        // Resolve custom variables in filename
        if (entry != null) {
            for (java.util.Map.Entry<String, String> varEntry : entry.getCustomVariables().entrySet()) {
                String placeholder = "${" + varEntry.getKey() + "}";
                if (result.contains(placeholder)) {
                    result = result.replace(placeholder, varEntry.getValue());
                }
            }
        }

        return result;
    }

    public static String toCamelCase(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }

        if (input.matches("[A-Z][a-zA-Z0-9]*")) {
            return Character.toLowerCase(input.charAt(0)) + input.substring(1);
        }

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

    public static String toKebabCase(String input) {
        if (input == null || input.isEmpty()) return input;

        String spaced = input.replaceAll("([a-z])([A-Z])", "$1 $2");

        spaced = spaced.replaceAll("[_\\s]+", " ").trim();

        String[] words = spaced.split(" ");

        return String.join("-", Arrays.stream(words)
                .map(String::toLowerCase)
                .toArray(String[]::new));
    }

}
