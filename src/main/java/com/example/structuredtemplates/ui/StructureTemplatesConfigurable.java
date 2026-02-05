package com.example.structuredtemplates.ui;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import com.intellij.openapi.util.JDOMUtil;
import com.intellij.util.xmlb.XmlSerializer;
import org.jdom.Element;
import org.jdom.CDATA;


import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.util.IconUtil;
import com.intellij.util.ui.JBUI;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class StructureTemplatesConfigurable implements SearchableConfigurable {

    private final Project project;

    private JPanel mainPanel;
    private JTree tree;
    private DefaultTreeModel treeModel;
    private DefaultMutableTreeNode rootNode;

    private JButton addTemplateButton;
    private JButton addFolderButton;
    private JButton addFileButton;
    private JButton removeNodeButton;
    private JButton importTemplatesButton;
    private JButton exportTemplatesButton;


    private List<StructureTemplate> workingTemplates;

    public StructureTemplatesConfigurable(Project project) {
        this.project = project;
    }

    @Override
    public @NotNull String getId() {
        return "com.example.structuredtemplates.StructureTemplatesConfigurable";
    }

    @Override
    public @Nls(capitalization = Nls.Capitalization.Title) String getDisplayName() {
        return "Structure Templates";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());

        rootNode = new DefaultMutableTreeNode("Templates");
        treeModel = new DefaultTreeModel(rootNode);

        tree = new JTree(treeModel);
        installContextMenu();
        tree.setRootVisible(true);
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    renameSelectedNode();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(tree);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        addTemplateButton = new JButton("Add Template");
        addFolderButton = new JButton("Add Folder");
        addFileButton = new JButton("Add File");
        removeNodeButton = new JButton("Remove Node");

        buttonPanel.add(addTemplateButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(addFolderButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(addFileButton);
        buttonPanel.add(Box.createVerticalStrut(5));
        buttonPanel.add(removeNodeButton);

        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT));

        importTemplatesButton = new JButton("Import Templates...");
        exportTemplatesButton = new JButton("Export Templates...");
        topBar.add(importTemplatesButton);
        topBar.add(exportTemplatesButton);

        mainPanel.add(topBar, BorderLayout.NORTH);
        mainPanel.add(buttonPanel, BorderLayout.EAST);

        initData();
        initListeners();

        return mainPanel;
    }

    private void installContextMenu() {
        JPopupMenu menu = new JPopupMenu();

        JMenuItem renameItem = new JMenuItem("Rename");
        renameItem.addActionListener(e -> renameSelectedNode());
        menu.add(renameItem);

        JMenuItem changeTemplateItem = new JMenuItem("Change File Template...");
        changeTemplateItem.addActionListener(e -> changeTemplateForSelectedNode());
        menu.add(changeTemplateItem);

        JMenuItem changeIconItem = new JMenuItem("Change Icon...");
        changeIconItem.addActionListener(e -> onChangeIcon());
        menu.add(changeIconItem);

        JMenuItem removeItem = new JMenuItem("Remove");
        removeItem.addActionListener(e -> onRemoveNode());
        menu.add(removeItem);

        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mousePressed(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }

            @Override
            public void mouseReleased(java.awt.event.MouseEvent e) {
                if (e.isPopupTrigger()) showMenu(e);
            }

            private void showMenu(java.awt.event.MouseEvent e) {
                TreePath path = tree.getPathForLocation(e.getX(), e.getY());
                if (path == null) return;

                tree.setSelectionPath(path);

                // Only show "Change File Template" for FILE nodes
                Object obj = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
                changeTemplateItem.setVisible(obj instanceof StructureEntry &&
                        ((StructureEntry) obj).getType() == StructureEntryType.FILE);

                changeIconItem.setVisible(obj instanceof StructureTemplate);

                menu.show(tree, e.getX(), e.getY());
            }
        });
    }

    private void initData() {
        TemplateSettings settings = TemplateSettings.getInstance(project);
        workingTemplates = new ArrayList<>(settings.getTemplates());
        rebuildTree();
    }

    private void rebuildTree() {
        rootNode.removeAllChildren();
        if (workingTemplates != null) {
            for (StructureTemplate template : workingTemplates) {
                DefaultMutableTreeNode templateNode = new DefaultMutableTreeNode(template);
                rootNode.add(templateNode);
                for (StructureEntry entry : template.getEntries()) {
                    templateNode.add(buildNode(entry));
                }
            }
        }
        treeModel.reload();

        tree.setCellRenderer(new javax.swing.tree.DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode) {
                    Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
                    if (userObject instanceof StructureTemplate) {
                        StructureTemplate st = (StructureTemplate) userObject;
                        Icon icon = getIconByPath(st.getIconPath());
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
        });
    }

    private DefaultMutableTreeNode buildNode(StructureEntry entry) {
        DefaultMutableTreeNode node = new DefaultMutableTreeNode(entry);
        if (entry.getType() == StructureEntryType.FOLDER) {
            for (StructureEntry child : entry.getChildren()) {
                node.add(buildNode(child));
            }
        }
        return node;
    }

    private void initListeners() {
        addTemplateButton.addActionListener(e -> onAddTemplate());
        addFolderButton.addActionListener(e -> onAddFolder());
        addFileButton.addActionListener(e -> onAddFile());
        removeNodeButton.addActionListener(e -> onRemoveNode());
        importTemplatesButton.addActionListener(e -> onImportTemplates());
        exportTemplatesButton.addActionListener(e -> onExportTemplates());
    }

    private void onChangeIcon() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null) return;
        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof StructureTemplate)) return;

        StructureTemplate template = (StructureTemplate) userObject;
        String iconPath = chooseIcon();
        if (iconPath != null) {
            template.setIconPath(iconPath);
            treeModel.nodeChanged(selectedNode);
        }
    }

    private String chooseIcon() {
        List<String> iconNames = new ArrayList<>();
        // Using reflection to get some icons from AllIcons.Nodes as examples
        try {
            for (Field field : AllIcons.Nodes.class.getDeclaredFields()) {
                if (field.getType().equals(javax.swing.Icon.class)) {
                    iconNames.add("AllIcons.Nodes." + field.getName());
                }
            }
        } catch (Exception e) {
            // fallback or ignore
        }

        if (iconNames.isEmpty()) {
            iconNames.add("AllIcons.Nodes.Folder");
            iconNames.add("AllIcons.Nodes.Package");
            iconNames.add("AllIcons.Nodes.Class");
            iconNames.add("AllIcons.Nodes.Function");
        }

        JBList<String> list = new JBList<>(iconNames);
        list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
            JBLabel label = new JBLabel(value);
            Icon icon = getIconByPath(value);
            if (icon != null) {
                label.setIcon(icon);
            }
            if (isSelected) {
                label.setBackground(list1.getSelectionBackground());
                label.setForeground(list1.getSelectionForeground());
                label.setOpaque(true);
            }
            return label;
        });

        DialogBuilder builder = new DialogBuilder(mainPanel);
        builder.setTitle("Choose Icon");
        builder.setCenterPanel(new JScrollPane(list));
        if (builder.showAndGet()) {
            return list.getSelectedValue();
        }
        return null;
    }

    private Icon getIconByPath(String path) {
        if (path == null || path.isEmpty()) return null;
        if (path.startsWith("AllIcons.")) {
            String[] parts = path.split("\\.");
            if (parts.length == 3) {
                try {
                    Class<?> clazz = null;
                    if (parts[1].equals("Nodes")) clazz = AllIcons.Nodes.class;
                    else if (parts[1].equals("Actions")) clazz = AllIcons.Actions.class;
                    
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

    private void onAddTemplate() {
        String name = JOptionPane.showInputDialog(mainPanel, "Template name:", "New Template", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }
        StructureTemplate template = new StructureTemplate(name.trim());
        String iconPath = chooseIcon();
        if (iconPath != null) {
            template.setIconPath(iconPath);
        }
        workingTemplates.add(template);

        DefaultMutableTreeNode templateNode = new DefaultMutableTreeNode(template);
        rootNode.add(templateNode);
        treeModel.reload(rootNode);
        tree.scrollPathToVisible(new TreePath(templateNode.getPath()));
    }

    private void onAddFolder() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null || selectedNode == rootNode) {
            JOptionPane.showMessageDialog(mainPanel, "Select a template or folder to add a folder under.");
            return;
        }

        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof StructureTemplate) && !(userObject instanceof StructureEntry)) {
            return;
        }

        String name = JOptionPane.showInputDialog(mainPanel, "Folder name:", "New Folder", JOptionPane.PLAIN_MESSAGE);
        if (name == null || name.trim().isEmpty()) {
            return;
        }

        StructureEntry folderEntry = new StructureEntry(name.trim(), StructureEntryType.FOLDER);

        if (userObject instanceof StructureTemplate) {
            StructureTemplate template = (StructureTemplate) userObject;
            template.addEntry(folderEntry);
        } else {
            StructureEntry parentEntry = (StructureEntry) userObject;
            if (parentEntry.getType() != StructureEntryType.FOLDER) {
                JOptionPane.showMessageDialog(mainPanel, "You can only add folders under templates or folders.");
                return;
            }
            parentEntry.addChild(folderEntry);
        }

        DefaultMutableTreeNode folderNode = new DefaultMutableTreeNode(folderEntry);
        selectedNode.add(folderNode);
        treeModel.reload(selectedNode);
        tree.scrollPathToVisible(new TreePath(folderNode.getPath()));
    }

    private void onAddFile() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null || selectedNode == rootNode) {
            JOptionPane.showMessageDialog(mainPanel, "Select a template or folder to add a file under.");
            return;
        }

        Object userObject = selectedNode.getUserObject();
        if (!(userObject instanceof StructureTemplate) && !(userObject instanceof StructureEntry)) {
            return;
        }

        String fileName = JOptionPane.showInputDialog(mainPanel, "File name:", "New File", JOptionPane.PLAIN_MESSAGE);
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        String templateName = chooseFileTemplateName();
        if (templateName == null) {
            return; // cancelled
        }

        StructureEntry fileEntry = new StructureEntry(fileName.trim(), templateName);

        if (userObject instanceof StructureTemplate) {
            StructureTemplate template = (StructureTemplate) userObject;
            template.addEntry(fileEntry);
        } else {
            StructureEntry parentEntry = (StructureEntry) userObject;
            if (parentEntry.getType() != StructureEntryType.FOLDER) {
                JOptionPane.showMessageDialog(mainPanel, "You can only add files under templates or folders.");
                return;
            }
            parentEntry.addChild(fileEntry);
        }

        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileEntry);
        selectedNode.add(fileNode);
        treeModel.reload(selectedNode);
        tree.scrollPathToVisible(new TreePath(fileNode.getPath()));
    }

    private String chooseFileTemplateName() {
        FileTemplateManager manager = FileTemplateManager.getInstance(project);
        FileTemplate[] templates = manager.getAllTemplates();
        if (templates.length == 0) {
            JOptionPane.showMessageDialog(mainPanel, "No file templates available in this project.");
            return null;
        }

        String[] names = new String[templates.length];
        for (int i = 0; i < templates.length; i++) {
            names[i] = templates[i].getName();
        }

        JComboBox<String> comboBox = new JComboBox<>(names);
        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                comboBox,
                "Choose File Template",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            return (String) comboBox.getSelectedItem();
        }
        return null;
    }

    private void onRemoveNode() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();
        if (selectedNode == null || selectedNode == rootNode) {
            return;
        }

        Object userObject = selectedNode.getUserObject();
        DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent();
        Object parentObject = parentNode.getUserObject();

        if (userObject instanceof StructureTemplate) {
            workingTemplates.remove(userObject);
        } else if (userObject instanceof StructureEntry) {
            StructureEntry entry = (StructureEntry) userObject;
            if (parentObject instanceof StructureTemplate) {
                ((StructureTemplate) parentObject).removeEntry(entry);
            } else if (parentObject instanceof StructureEntry) {
                ((StructureEntry) parentObject).removeChild(entry);
            }
        }

        parentNode.remove(selectedNode);
        treeModel.reload(parentNode);
    }

    private DefaultMutableTreeNode getSelectedNode() {
        TreePath path = tree.getSelectionPath();
        if (path == null) {
            return null;
        }
        return (DefaultMutableTreeNode) path.getLastPathComponent();
    }

    @Override
    public boolean isModified() {
        TemplateSettings settings = TemplateSettings.getInstance(project);
        List<StructureTemplate> stored = settings.getTemplates();
        return !stored.equals(workingTemplates);
    }

    @Override
    public void apply() {
        TemplateSettings settings = TemplateSettings.getInstance(project);
        settings.setTemplates(workingTemplates);
    }

    @Override
    public void reset() {
        initData();
    }

    @Override
    public void disposeUIResources() {
        mainPanel = null;
        tree = null;
        treeModel = null;
        rootNode = null;
        addTemplateButton = null;
        addFolderButton = null;
        addFileButton = null;
        removeNodeButton = null;
    }

    private void renameSelectedNode() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null || node == rootNode) return;

        Object obj = node.getUserObject();

        String currentName;
        if (obj instanceof StructureTemplate) {
            currentName = ((StructureTemplate) obj).getName();
        } else if (obj instanceof StructureEntry) {
            currentName = ((StructureEntry) obj).getName();
        } else {
            return;
        }

        String newName = JOptionPane.showInputDialog(
                mainPanel,
                "Enter new name:",
                currentName
        );

        if (newName == null || newName.trim().isEmpty()) return;

        newName = newName.trim();

        if (obj instanceof StructureTemplate) {
            ((StructureTemplate) obj).setName(newName);
        } else if (obj instanceof StructureEntry) {
            ((StructureEntry) obj).setName(newName);
        }

        treeModel.nodeChanged(node);
    }

    private void changeTemplateForSelectedNode() {
        DefaultMutableTreeNode node = getSelectedNode();
        if (node == null) return;

        Object obj = node.getUserObject();
        if (!(obj instanceof StructureEntry)) return;

        StructureEntry entry = (StructureEntry) obj;
        if (entry.getType() != StructureEntryType.FILE) return;

        String newTemplate = chooseFileTemplateName();
        if (newTemplate == null) return;

        entry.setFileTemplateName(newTemplate);

        treeModel.nodeChanged(node);
    }

    private void onImportTemplates() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Template Pack");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        // 🔥 Only show .xml files
        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter( new javax.swing.filechooser.FileNameExtensionFilter("XML Files (*.xml)", "xml") );

        int result = chooser.showOpenDialog(mainPanel);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (file == null || !file.exists()) {
            JOptionPane.showMessageDialog(mainPanel, "Invalid file selected.");
            return;
        }

        try {
            Element root = JDOMUtil.load(file);

            // -----------------------------
            // IMPORT FILE TEMPLATES
            // -----------------------------
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
                        existing.setText(content); // overwrite
                    }
                }
            }

            // -----------------------------
            // IMPORT STRUCTURE TEMPLATES
            // -----------------------------
            Element structureEl = root.getChild("structureTemplates");
            if (structureEl != null) {
                TemplateSettings.State importedState =
                        XmlSerializer.deserialize(structureEl, TemplateSettings.State.class);

                TemplateSettings settings = TemplateSettings.getInstance(project);
                settings.loadState(importedState);
            }

            // Refresh UI
            initData();
            treeModel.reload();

            JOptionPane.showMessageDialog(mainPanel, "Template pack imported successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Failed to import: " + ex.getMessage());
        }
    }

    private void onExportTemplates() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Export Template Pack");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

        chooser.setAcceptAllFileFilterUsed(false);
        chooser.addChoosableFileFilter(
                new FileNameExtensionFilter("XML Files (*.xml)", "xml")
        );

        int result = chooser.showSaveDialog(mainPanel);
        if (result != JFileChooser.APPROVE_OPTION) return;

        File file = chooser.getSelectedFile();
        if (file == null) {
            JOptionPane.showMessageDialog(mainPanel, "Invalid file selected.");
            return;
        }

        if (!file.getName().toLowerCase().endsWith(".xml")) {
            file = new File(file.getAbsolutePath() + ".xml");
        }

        try {
            TemplateSettings settings = TemplateSettings.getInstance(project);
            TemplateSettings.State state = settings.getState();

            Element root = new Element("templatePack");

            // -----------------------------
            // EXPORT ONLY USED FILE TEMPLATES
            // -----------------------------
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

            // -----------------------------
            // EXPORT STRUCTURE TEMPLATES
            // -----------------------------
            Element structureEl = XmlSerializer.serialize(state);
            structureEl.setName("structureTemplates");
            root.addContent(structureEl);

            // Write to disk
            String xml = JDOMUtil.writeElement(root);

            Files.write(file.toPath(), xml.getBytes(StandardCharsets.UTF_8));

            JOptionPane.showMessageDialog(mainPanel, "Template pack exported successfully.");

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Failed to export: " + ex.getMessage());
        }
    }

    private Set<String> collectUsedFileTemplateNames(TemplateSettings.State state) {
        Set<String> names = new HashSet<>();

        if (state == null || state.templates == null) {
            return names;
        }

        for (TemplateSettings.TemplateState template : state.templates) {
            for (TemplateSettings.EntryState entry : template.entries) {
                collectFromEntry(entry, names);
            }
        }

        return names;
    }

    private void collectFromEntry(TemplateSettings.EntryState entry, Set<String> names) {
        if (entry == null) return;

        // Only FILE entries have fileTemplateName
        if (StructureEntryType.FILE.name().equals(entry.type)) {
            if (entry.fileTemplateName != null && !entry.fileTemplateName.isEmpty()) {
                names.add(entry.fileTemplateName);
            }
        }

        // Recurse into children
        if (entry.children != null) {
            for (TemplateSettings.EntryState child : entry.children) {
                collectFromEntry(child, names);
            }
        }
    }


}
