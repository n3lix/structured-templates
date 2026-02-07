package com.example.structuredtemplates.ui;

import com.example.structuredtemplates.model.StructureEntry;
import com.example.structuredtemplates.model.StructureEntryType;
import com.example.structuredtemplates.model.StructureTemplate;
import com.example.structuredtemplates.settings.TemplateSettings;
import com.example.structuredtemplates.util.IconUtils;
import com.example.structuredtemplates.util.TemplateImportExportManager;
import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.options.SearchableConfigurable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.ui.components.JBLabel;
import com.intellij.ui.components.JBList;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.TreeSpeedSearch;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.tree.TreeUtil;
import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class StructureTemplatesConfigurable implements SearchableConfigurable {

    private final Project project;

    private JPanel mainPanel;
    private Tree tree;
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
        return "Structured Templates";
    }

    @Override
    public @Nullable JComponent createComponent() {
        mainPanel = new JPanel(new BorderLayout());

        rootNode = new DefaultMutableTreeNode("Templates");
        treeModel = new DefaultTreeModel(rootNode);
        tree = new Tree(treeModel);
        tree.setCellRenderer(new TemplateTreeCellRenderer());
        tree.setRootVisible(false);
        tree.setShowsRootHandles(true);
        new TreeSpeedSearch(tree) {
            @Override
            protected String getElementText(Object element) {
                return element != null ? element.toString() : null;
            }
        };

        installContextMenu();
        tree.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (e.getClickCount() == 2) {
                    renameSelectedNode();
                }
            }
        });

        InputMap inputMap = tree.getInputMap(JComponent.WHEN_FOCUSED);
        ActionMap actionMap = tree.getActionMap();

        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "deleteNode");

        actionMap.put("deleteNode", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                TreePath path = tree.getSelectionPath();
                if (path == null) return;

                onRemoveNode();
            }
        });

        JBScrollPane scrollPane = new JBScrollPane(tree);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));

        addTemplateButton = new JButton("Add Template");
        addFolderButton = new JButton("Add Folder");
        addFileButton = new JButton("Add File");
        removeNodeButton = new JButton("Remove Node");

        addFolderButton.setEnabled(false);
        addFileButton.setEnabled(false);
        removeNodeButton.setEnabled(false);

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

        menu.addSeparator();

        JMenuItem createNewTemplate = new JMenuItem("Create New Template");
        createNewTemplate.addActionListener(e -> onAddTemplate());
        menu.add(createNewTemplate);

        JMenuItem addFolder = new JMenuItem("Add Folder");
        addFolder.addActionListener(e -> onAddFolder());
        menu.add(addFolder);

        JMenuItem addFile = new JMenuItem("Add File");
        addFile.addActionListener(e -> onAddFile());
        menu.add(addFile);

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
                TreePath path = tree.getClosestPathForLocation(e.getX(), e.getY());
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
        TreeUtil.expandAll(tree);
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
        tree.addTreeSelectionListener(e -> onTreeNodeSelection());
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
        if (!(userObject instanceof StructureTemplate template)) return;

        String iconPath = chooseIcon();
        if (iconPath != null) {
            template.setIconPath(iconPath);
            treeModel.nodeChanged(selectedNode);
        }
    }

    private String chooseIcon() {
        List<String> iconNames = IconUtils.getAllIconsNodesPaths();

        JBList<String> list = new JBList<>(iconNames);
        list.setCellRenderer((list1, value, index, isSelected, cellHasFocus) -> {
            JBLabel label = new JBLabel(value);
            Icon icon = IconUtils.getIconByPath(value);
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
        builder.setCenterPanel(new JBScrollPane(list));
        if (builder.showAndGet()) {
            return list.getSelectedValue();
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
        reloadTree(rootNode, false);

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

        if (userObject instanceof StructureTemplate template) {
            template.addEntry(folderEntry);
        } else {
            StructureEntry parentEntry = (StructureEntry) userObject;
            if (parentEntry.getType() != StructureEntryType.FOLDER) {
                selectedNode = (DefaultMutableTreeNode) selectedNode.getParent(); // if selected node is file then create the file udner its parent.
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
            return; // unknown node
        } else if (userObject instanceof StructureEntry parentEntry && parentEntry.getType() != StructureEntryType.FOLDER) {
            selectedNode = (DefaultMutableTreeNode) selectedNode.getParent(); // if selected node is file then create the file udner its parent.
        }

        String fileName = JOptionPane.showInputDialog(mainPanel, "File name:", "New File", JOptionPane.PLAIN_MESSAGE);
        if (fileName == null || fileName.trim().isEmpty()) {
            return;
        }

        FileTemplate fileTemplate = chooseFileTemplateName();
        if (fileTemplate == null) {
            return; // cancelled
        }

        StructureEntry fileEntry = new StructureEntry(fileName.trim(), fileTemplate.getName(), fileTemplate.getExtension());

        if (userObject instanceof StructureTemplate template) {
            template.addEntry(fileEntry);
        } else {
            StructureEntry parentEntry = (StructureEntry) userObject;
            parentEntry.addChild(fileEntry);
        }

        DefaultMutableTreeNode fileNode = new DefaultMutableTreeNode(fileEntry);
        selectedNode.add(fileNode);
        treeModel.reload(selectedNode);
        tree.scrollPathToVisible(new TreePath(fileNode.getPath()));
    }

    private FileTemplate chooseFileTemplateName() {
        FileTemplateManager manager = FileTemplateManager.getInstance(project);
        FileTemplate[] templates = manager.getTemplates(FileTemplateManager.DEFAULT_TEMPLATES_CATEGORY);

        if (templates.length == 0) {
            JOptionPane.showMessageDialog(mainPanel, "No file templates available in this project.");
            return null;
        }

        ComboBox<FileTemplate> comboBox = new ComboBox<>(templates);
        comboBox.setRenderer(new ComboBoxRenderer());
        int result = JOptionPane.showConfirmDialog(
                mainPanel,
                comboBox,
                "Choose File Template",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );
        if (result == JOptionPane.OK_OPTION) {
            return (FileTemplate) comboBox.getSelectedItem();
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
        } else if (userObject instanceof StructureEntry entry) {
            if (parentObject instanceof StructureTemplate) {
                ((StructureTemplate) parentObject).removeEntry(entry);
            } else if (parentObject instanceof StructureEntry) {
                ((StructureEntry) parentObject).removeChild(entry);
            }
        }

        parentNode.remove(selectedNode);
        reloadTree(parentNode, false);

        tree.clearSelection();
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
        if (!(obj instanceof StructureEntry entry)) return;

        if (entry.getType() != StructureEntryType.FILE) return;

        FileTemplate newTemplate = chooseFileTemplateName();
        if (newTemplate == null) return;

        entry.setFileTemplateName(newTemplate.getName());

        treeModel.nodeChanged(node);
    }

    private void onImportTemplates() {
        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Import Template Pack");
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

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
            new TemplateImportExportManager(project).importFromFile(file);

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
            new TemplateImportExportManager(project).exportToFile(file);
            JOptionPane.showMessageDialog(mainPanel, "Template pack exported successfully.");
        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(mainPanel, "Failed to export: " + ex.getMessage());
        }
    }

    private void onTreeNodeSelection() {
        DefaultMutableTreeNode selectedNode = getSelectedNode();

        if (selectedNode == null) {
            disableAllButtons();
            return;
        }

        Object userObject = selectedNode.getUserObject();

        if (!(userObject instanceof StructureTemplate) && !(userObject instanceof StructureEntry)) {
            disableAllButtons();
            return;
        }

        enableButtons(true, true);
    }

    private void disableAllButtons() {
        addFolderButton.setEnabled(false);
        addFileButton.setEnabled(false);
        removeNodeButton.setEnabled(false);
    }

    private void enableButtons(boolean enableFolder, boolean enableFile) {
        addFolderButton.setEnabled(enableFolder);
        addFileButton.setEnabled(enableFile);
        removeNodeButton.setEnabled(true);
    }

    private void reloadTree(TreeNode treeNode, boolean collapseAll) {
        if (collapseAll) {
            treeModel.reload(treeNode);
            return;
        }

        Enumeration<TreePath> expanded = tree.getExpandedDescendants(new TreePath(treeNode));
        treeModel.reload(treeNode);
        if (expanded != null) {
            while (expanded.hasMoreElements()) {
                tree.expandPath(expanded.nextElement());
            }
        }
    }
}
