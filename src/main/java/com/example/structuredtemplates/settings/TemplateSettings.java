package com.example.structuredtemplates.settings;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.components.StoragePathMacros;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Project-level storage for structure templates.
 */
@State(
        name = "StructuredTemplatesSettings",
        storages = @Storage("structured-templates.xml")
//        storages = @Storage(StoragePathMacros.NON_ROAMABLE_FILE)
)
@Service(Service.Level.PROJECT)
public final class TemplateSettings implements PersistentStateComponent<TemplateSettings.State> {

    public static class State {
        public List<TemplateState> templates = new ArrayList<>();
    }

    public static class TemplateState {
        public String name;
        public String iconPath;
        public List<EntryState> entries = new ArrayList<>();
    }

    public static class EntryState {
        public String name;
        public String type; // FOLDER or FILE
        public String fileTemplateName;
        public List<EntryState> children = new ArrayList<>();
    }

    private State state = new State();

    public TemplateSettings() {
        ensureDefaultTemplate();
    }

    public static TemplateSettings getInstance(Project project) {
        return project.getService(TemplateSettings.class);
    }

    @Override
    public @Nullable State getState() {
        if (state == null) {
            state = new State();
        }
        ensureDefaultTemplate();
        return state;
    }

    @Override
    public void loadState(@NotNull State state) {
        this.state = state;
        ensureDefaultTemplate();
    }

    private void ensureDefaultTemplate() {
        if (state.templates == null) {
            state.templates = new ArrayList<>();
        }
        if (state.templates.isEmpty()) {
            TemplateState componentTemplate = new TemplateState();
            componentTemplate.iconPath = "AllIcons.Nodes.ModuleGroup";
            componentTemplate.name = "Component";

            EntryState jsFile = new EntryState();
            jsFile.name = "component.js";
            jsFile.type = StructureEntryType.FILE.name();
            jsFile.fileTemplateName = "JavaScript File"; // adjust to an existing template name

            EntryState cssFile = new EntryState();
            cssFile.name = "component.css";
            cssFile.type = StructureEntryType.FILE.name();
            cssFile.fileTemplateName = "CSS File"; // adjust to an existing template name

            componentTemplate.entries.add(jsFile);
            componentTemplate.entries.add(cssFile);

            state.templates.add(componentTemplate);
        }
    }

    public List<StructureTemplate> getTemplates() {
        List<StructureTemplate> result = new ArrayList<>();
        if (state.templates == null) {
            return result;
        }
        for (TemplateState ts : state.templates) {
            StructureTemplate template = new StructureTemplate(ts.name, ts.iconPath);
            for (EntryState es : ts.entries) {
                template.addEntry(fromEntryState(es));
            }
            result.add(template);
        }
        return result;
    }

    public void setTemplates(List<StructureTemplate> templates) {
        State newState = new State();
        for (StructureTemplate template : templates) {
            TemplateState ts = new TemplateState();
            ts.name = template.getName();
            ts.iconPath = template.getIconPath();
            for (StructureEntry entry : template.getEntries()) {
                ts.entries.add(toEntryState(entry));
            }
            newState.templates.add(ts);
        }
        this.state = newState;
    }

    private static EntryState toEntryState(StructureEntry entry) {
        EntryState es = new EntryState();
        es.name = entry.getName();
        es.type = entry.getType().name();
        es.fileTemplateName = entry.getFileTemplateName();
        if (entry.getChildren() != null) {
            for (StructureEntry child : entry.getChildren()) {
                es.children.add(toEntryState(child));
            }
        }
        return es;
    }

    private static StructureEntry fromEntryState(EntryState es) {
        StructureEntryType type = StructureEntryType.valueOf(es.type);
        StructureEntry entry;
        if (type == StructureEntryType.FILE) {
            entry = new StructureEntry(es.name, es.fileTemplateName);
        } else {
            entry = new StructureEntry(es.name, type);
        }
        if (es.children != null) {
            for (EntryState childState : es.children) {
                entry.addChild(fromEntryState(childState));
            }
        }
        return entry;
    }
}
