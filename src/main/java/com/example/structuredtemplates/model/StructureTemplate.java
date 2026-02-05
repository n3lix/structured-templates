package com.example.structuredtemplates.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StructureTemplate {

    private String name;
    private String iconPath;
    private final List<StructureEntry> entries = new ArrayList<>();

    public StructureTemplate(String name) {
        this.name = name;
    }

    public StructureTemplate(String name, String iconPath) {
        this.name = name;
        this.iconPath = iconPath;
    }

    public StructureTemplate() {
    }

    public String getName() {
        return name;
    }

    public String getIconPath() {
        return iconPath;
    }

    public List<StructureEntry> getEntries() {
        return entries;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setIconPath(String iconPath) {
        this.iconPath = iconPath;
    }

    public void addEntry(StructureEntry entry) {
        entries.add(entry);
    }

    public void removeEntry(StructureEntry entry) {
        entries.remove(entry);
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof StructureTemplate)) return false;
        StructureTemplate that = (StructureTemplate) o;
        return Objects.equals(name, that.name) &&
                Objects.equals(iconPath, that.iconPath) &&
                Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, iconPath, entries);
    }
}
