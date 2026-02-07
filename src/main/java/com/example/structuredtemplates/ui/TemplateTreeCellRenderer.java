package com.example.structuredtemplates.ui;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.util.IconUtils;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class TemplateTreeCellRenderer extends ColoredTreeCellRenderer {

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (node.isRoot()) {
                append(userObject != null ? userObject.toString() : "");
                setIcon(AllIcons.Nodes.ConfigFolder);
            } else if (userObject instanceof StructureTemplate) {
                StructureTemplate st = (StructureTemplate) userObject;
                append(st.getName() != null ? st.getName() : "");
                Icon icon = IconUtils.getIconByPath(st.getIconPath());
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setIcon(AllIcons.Nodes.ModuleGroup);
                }
            } else if (userObject instanceof StructureEntry) {
                StructureEntry se = (StructureEntry) userObject;
                append(se.getName() != null ? se.getName() : "");
                if (se.getType() == StructureEntryType.FILE && se.getFileTemplateName() != null) {
                    append(" (" + se.getFileTemplateName() + ")", SimpleTextAttributes.GRAYED_ATTRIBUTES);
                }

                if (se.getType() == StructureEntryType.FOLDER) {
                    setIcon(AllIcons.Nodes.Folder);
                } else {
                    String ext = se.getExtension();

                    if (ext != null && !ext.isEmpty()) {
                        FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(se.getExtension());
                        setIcon(fileType.getIcon());
                    } else {
                        setIcon(AllIcons.FileTypes.Text);
                    }
                }
            }
        }
    }
}
