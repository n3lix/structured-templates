package com.example.structuredtemplates.ui;

import com.intellij.icons.AllIcons;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.FileTypeManager;

import javax.swing.*;
import java.awt.*;

public class ComboBoxRenderer implements ListCellRenderer<FileTemplate> {

    private final JLabel label = new JLabel();

    @Override
    public Component getListCellRendererComponent(
            JList<? extends FileTemplate> list,
            FileTemplate value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        label.setOpaque(true);

        if (isSelected) {
            label.setBackground(list.getSelectionBackground());
            label.setForeground(list.getSelectionForeground());
        } else {
            label.setBackground(list.getBackground());
            label.setForeground(list.getForeground());
        }

        if (value != null) {
            label.setText(value.getName());

            String ext = value.getExtension();

            if (!ext.isEmpty()) {
                FileType fileType = FileTypeManager.getInstance()
                        .getFileTypeByExtension(ext);

                label.setIcon(fileType.getIcon());
            } else {
                label.setIcon(AllIcons.FileTypes.Text);
            }
        }

        return label;
    }
}
