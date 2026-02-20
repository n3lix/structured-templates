package com.example.structuredtemplates.ui;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.util.IconUtils;
import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.ui.ColoredTreeCellRenderer;
import com.intellij.ui.SimpleTextAttributes;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;

public class TemplateTreeCellRenderer extends ColoredTreeCellRenderer {
    private final Project project;

    public TemplateTreeCellRenderer(Project project){
        this.project = project;
    }

    @Override
    public void customizeCellRenderer(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        if (value instanceof DefaultMutableTreeNode node) {
            Object userObject = node.getUserObject();
            if (node.isRoot()) {
                append(userObject != null ? userObject.toString() : "");
                setIcon(AllIcons.Nodes.ConfigFolder);
            } else if (userObject instanceof StructureTemplate st) {
                append(st.getName() != null ? st.getName() : "");
                Icon icon = IconUtils.getIconByPath(st.getIconPath());
                if (icon != null) {
                    setIcon(icon);
                } else {
                    setIcon(AllIcons.Nodes.ModuleGroup);
                }
            } else if (userObject instanceof StructureEntry se) {
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
                        //if the entry doesn't have an extension (old version of the plugin) try to detect it from File Template

                        FileTemplateManager manager = FileTemplateManager.getInstance(project);
                        FileTemplate[] templates = manager.getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);

                        FileTemplate matched = null;
                        for (FileTemplate t : templates) {
                            if (t.getName().equals(se.getFileTemplateName())) {
                                matched = t;
                                break;
                            }
                        }

                        if (matched != null) {
                            String templateExt = matched.getExtension();
                            if (!templateExt.isEmpty()) {
                                se.setExtension(templateExt); // set the extension to match the template
                                FileType fileType = FileTypeManager.getInstance().getFileTypeByExtension(templateExt);
                                if (fileType.getIcon() != null) {
                                    setIcon(fileType.getIcon());
                                    return;
                                }
                            }
                        }

                        //fallback to Text icon if nothing matched
                        setIcon(AllIcons.FileTypes.Text);
                    }
                }
            }
        }
    }
}
