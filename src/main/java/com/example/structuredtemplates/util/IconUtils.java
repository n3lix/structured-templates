package com.example.structuredtemplates.util;

import com.intellij.icons.AllIcons;
import javax.swing.*;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class IconUtils {

    public static Icon getIconByPath(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("AllIcons.")) {
            String[] parts = path.split("\\.");
            if (parts.length == 3) {
                try {
                    Class<?> clazz = null;
                    if (parts[1].equals("Nodes")) clazz = AllIcons.Nodes.class;
                    else if (parts[1].equals("Actions")) clazz = AllIcons.Actions.class;
                    else if (parts[1].equals("FileTypes")) clazz = AllIcons.FileTypes.class;

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

    public static List<String> getAllIconsNodesPaths() {
        List<String> iconNames = new ArrayList<>();
        try {
            for (Field field : AllIcons.Nodes.class.getDeclaredFields()) {
                if (field.getType().equals(javax.swing.Icon.class)) {
                    iconNames.add("AllIcons.Nodes." + field.getName());
                }
            }
        } catch (Exception e) {
            // ignore
        }
        if (iconNames.isEmpty()) {
            iconNames.add("AllIcons.Nodes.Folder");
            iconNames.add("AllIcons.Nodes.Package");
            iconNames.add("AllIcons.Nodes.Class");
            iconNames.add("AllIcons.Nodes.Function");
        }
        return iconNames;
    }
}
