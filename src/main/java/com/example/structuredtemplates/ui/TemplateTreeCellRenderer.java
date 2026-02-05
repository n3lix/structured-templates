package com.example.structuredtemplates.ui;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.util.IconUtils;
import com.intellij.icons.AllIcons;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import java.awt.*;

public class TemplateTreeCellRenderer extends DefaultTreeCellRenderer {

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
        if (value instanceof DefaultMutableTreeNode) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
            Object userObject = node.getUserObject();
            if (node.isRoot()) {
                setIcon(AllIcons.Nodes.ConfigFolder);
            } else if (userObject instanceof StructureTemplate) {
                StructureTemplate st = (StructureTemplate) userObject;
                Icon icon = IconUtils.getIconByPath(st.getIconPath());
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setIcon(AllIcons.Nodes.ModuleGroup);
                }
            } else if (userObject instanceof StructureEntry) {
                StructureEntry se = (StructureEntry) userObject;
                if (se.getType() == StructureEntryType.FOLDER) {
                    setIcon(AllIcons.Nodes.Folder);
                } else {
                    setIcon(AllIcons.FileTypes.Text);
                }
            }
        }
        return this;
    }
}
