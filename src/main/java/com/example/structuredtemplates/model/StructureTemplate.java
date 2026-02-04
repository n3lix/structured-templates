package com.example.structuredtemplates.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class StructureTemplate {

    private String name;
    private final List<StructureEntry> entries = new ArrayList<>();

    public StructureTemplate(String name) {
        this.name = name;
    }

    public StructureTemplate() {
    }

    public String getName() {
        return name;
    }

    public List<StructureEntry> getEntries() {
        return entries;
    }

    public void setName(String name) {
        this.name = name;
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
                Objects.equals(entries, that.entries);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, entries);
    }
}
