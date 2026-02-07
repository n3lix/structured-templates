package com.example.structuredtemplates.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StructureEntry {

    private String name;
    private StructureEntryType type;
    private String fileTemplateName;
    private String extension;
    private final List<StructureEntry> children = new ArrayList<>();

    public StructureEntry(String name, StructureEntryType type) {
        this.name = name;
        this.type = type;
    }

    public StructureEntry(String name, String fileTemplateName, String ext) {
        this.name = name;
        this.type = StructureEntryType.FILE;
        this.fileTemplateName = fileTemplateName;
        this.extension = ext;
    }

    public String getName() {
        return name;
    }

    public StructureEntryType getType() {
        return type;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setType(StructureEntryType type) {
        this.type = type;
    }

    public String getExtension() { return extension; }

    public void setExtension(String ext) { extension = ext; }

    public String getFileTemplateName() {
        return fileTemplateName;
    }

    public void setFileTemplateName(String fileTemplateName) {
        this.fileTemplateName = fileTemplateName;
    }

    public List<StructureEntry> getChildren() {
        return children;
    }

    public void addChild(StructureEntry entry) {
        children.add(entry);
    }

    public void removeChild(StructureEntry entry) {
        children.remove(entry);
    }

    @Override
    public String toString() {
        if (type == StructureEntryType.FILE && fileTemplateName != null) {
            return name + " (" + fileTemplateName + ")";
        }
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureEntry)) return false;
        StructureEntry that = (StructureEntry) o;
        return Objects.equals(name, that.name)
                && type == that.type
                && Objects.equals(fileTemplateName, that.fileTemplateName)
                && Objects.equals(children, that.children);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, type, fileTemplateName, children);
    }
}
