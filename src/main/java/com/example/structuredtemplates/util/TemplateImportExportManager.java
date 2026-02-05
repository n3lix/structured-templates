package com.example.structuredtemplates.util;

import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.CDATA;
import org.jdom.Element;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.HashSet;
import java.util.Set;

public class TemplateImportExportManager {

    private final Project project;

    public TemplateImportExportManager(Project project) {
        this.project = project;
    }

    public void importFromFile(File file) throws Exception {
        Element root = JDOMUtil.load(file);

        // Import File Templates
        Element fileTemplatesEl = root.getChild("fileTemplates");
        if (fileTemplatesEl != null) {
            FileTemplateManager ftm = FileTemplateManager.getInstance(project);
            for (Element tEl : fileTemplatesEl.getChildren("template")) {
                String name = tEl.getAttributeValue("name");
                String ext = tEl.getAttributeValue("extension");
                String content = tEl.getText();

                FileTemplate existing = ftm.getTemplate(name);
                if (existing == null) {
                    FileTemplate newTemplate = ftm.addTemplate(name, ext);
                    newTemplate.setText(content);
                } else {
                    existing.setText(content);
                }
            }
        }

        // Import Structure Templates
        Element structureEl = root.getChild("structureTemplates");
        if (structureEl != null) {
            TemplateSettings.State importedState =
                    XmlSerializer.deserialize(structureEl, TemplateSettings.State.class);
            TemplateSettings.getInstance(project).loadState(importedState);
        }
    }

    public void exportToFile(File file) throws IOException {
        TemplateSettings settings = TemplateSettings.getInstance(project);
        TemplateSettings.State state = settings.getState();

        Element root = new Element("templatePack");

        // Export only used file templates
        Set<String> usedNames = collectUsedFileTemplateNames(state);
        FileTemplateManager ftm = FileTemplateManager.getInstance(project);
        FileTemplate[] all = ftm.getAllTemplates();

        Element fileTemplatesEl = new Element("fileTemplates");
        for (FileTemplate t : all) {
            if (!usedNames.contains(t.getName())) continue;

            Element tEl = new Element("template");
            tEl.setAttribute("name", t.getName());
            tEl.setAttribute("extension", t.getExtension() == null ? "" : t.getExtension());
            tEl.addContent(new CDATA(t.getText()));
            fileTemplatesEl.addContent(tEl);
        }
        root.addContent(fileTemplatesEl);

        // Export structure templates
        Element structureEl = XmlSerializer.serialize(state);
        structureEl.setName("structureTemplates");
        root.addContent(structureEl);

        String xml = JDOMUtil.writeElement(root);
        Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));
    }

    private Set<String> collectUsedFileTemplateNames(TemplateSettings.State state) {
        Set<String> names = new HashSet<>();
        if (state == null || state.templates == null) return names;

        for (TemplateSettings.TemplateState template : state.templates) {
            for (TemplateSettings.EntryState entry : template.entries) {
                collectFromEntry(entry, names);
            }
        }
        return names;
    }

    private void collectFromEntry(TemplateSettings.EntryState entry, Set<String> names) {
        if (entry == null) return;
        if (StructureEntryType.FILE.name().equals(entry.type)) {
            if (entry.fileTemplateName != null && !entry.fileTemplateName.isEmpty()) {
                names.add(entry.fileTemplateName);
            }
        }
        if (entry.children != null) {
            for (TemplateSettings.EntryState child : entry.children) {
                collectFromEntry(child, names);
            }
        }
    }
}
