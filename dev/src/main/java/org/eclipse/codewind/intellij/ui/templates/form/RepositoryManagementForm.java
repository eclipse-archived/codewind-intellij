/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.codewind.intellij.ui.templates.form;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.TemplateUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.RepositoryInfo;
import org.eclipse.codewind.intellij.ui.form.WidgetUtils;
import org.eclipse.codewind.intellij.ui.form.WizardHeaderForm;
import org.eclipse.codewind.intellij.ui.templates.AbstractAddTemplateSourceWizardStep;
import org.eclipse.codewind.intellij.ui.templates.AddTemplateSourceWizardModel;
import org.eclipse.codewind.intellij.ui.templates.DetailsStep;
import org.eclipse.codewind.intellij.ui.templates.EditTemplateSourceWizard;
import org.eclipse.codewind.intellij.ui.templates.RepoEntry;
import org.eclipse.codewind.intellij.ui.templates.AddTemplateSourceWizard;
import org.eclipse.codewind.intellij.ui.templates.UrlStep;
import org.jetbrains.annotations.NotNull;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class RepositoryManagementForm {
    private JPanel contentPane;
    private JPanel rowPanel;
    private JTable repoViewer;
    private JButton addButton;
    private JButton removeButton;
    private JScrollPane tableScrollPane;
    private JPanel buttonPanel;
    private JLabel descLabel;
    private JTextPane descTextPane;
    private JLabel styleLabel;
    private JLabel urlLabel;
    private JPanel detailsPanel;
    private JTextPane styleTextPane;
    private JTextPane urlTextPane;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JTextPane descriptionTextPane;
    private JButton editButton;

    private List<RepositoryInfo> repoList; // Actual list of repo from PFE
    private CodewindConnection connection;
    private Project project;
    private List<RepoEntry> repoEntries; // Current edited set of repos (content of the table)

    private RepoViewerModel repoViewerModel;

    public RepositoryManagementForm(Project project, CodewindConnection connection, List<RepositoryInfo> repoList) {
        this.project = project;
        this.connection = connection;
        this.repoList = repoList;
    }

    public void initForm() {
        this.repoEntries = getRepoEntries(repoList);
        if (repoViewer instanceof RepoViewer) {
            ((RepoViewer)repoViewer).init();
            ((RepoViewer)repoViewer).setRepoEntries(this.repoEntries);
            if (repoEntries.size() > 0) {
                repoViewer.setRowSelectionInterval(0, 0);
            }
        }
    }

    private void createUIComponents() {
        WizardHeaderForm headerForm = new WizardHeaderForm(message("RepoMgmtDialogTitle"), message("RepoMgmtDialogMessage"));
        headerPanel = headerForm.getContentPane();
        descriptionTextPane = new JTextPane();
        repoViewer = new RepoViewer();
        tableScrollPane = new JBScrollPane(repoViewer);
        tableScrollPane.setPreferredSize(new Dimension(600, 200));
        addButton = new JButton();
        editButton = new JButton();
        removeButton = new JButton();

        descriptionTextPane.setText(message("RepoMgmtDescription"));
        addButton.setText(message("RepoMgmtAddButton"));
        editButton.setText(message("RepoMgmtEditButton"));
        removeButton.setText(message("RepoMgmtRemoveButton"));

        detailsPanel = new JPanel();
        descTextPane = WidgetUtils.createTextPane("", true);
        styleTextPane = WidgetUtils.createTextPane("", true);
        urlTextPane = WidgetUtils.createHyperlinkUsingTextPane("", true);

        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                List<AbstractAddTemplateSourceWizardStep> steps = new ArrayList<>();
                AddTemplateSourceWizardModel wizardModel = new AddTemplateSourceWizardModel();
                wizardModel.setRepoEntries(repoEntries);
                UrlStep urlStep = new UrlStep(project, connection, wizardModel);
                steps.add(urlStep);
                steps.add(new DetailsStep(project, connection, wizardModel));
                wizardModel.addSteps(steps);
                AddTemplateSourceWizard wizard = new AddTemplateSourceWizard(project, steps);
                urlStep.addListener(wizard);
                boolean rc = wizard.showAndGet();
                if (rc) {
                    RepoEntry repoEntry = wizard.getRepoEntry();
                    if (repoEntry != null) {
                        repoEntries.add(repoEntry);
                        repoViewerModel.fireTableDataChanged();
                    }
                }

            }
        });

        editButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int selectedIndex = repoViewer.getSelectedRow();
                RepoEntry selectedRepo = repoEntries.get(selectedIndex);
                List<AbstractAddTemplateSourceWizardStep> steps = new ArrayList<>();
                AddTemplateSourceWizardModel wizardModel = new AddTemplateSourceWizardModel();
                wizardModel.setRepoEntries(repoEntries);
                wizardModel.setNameValue(selectedRepo.name);
                wizardModel.setDescriptionValue(selectedRepo.description);
                wizardModel.setIsEdit(true);
                wizardModel.setUrlValue(selectedRepo.url);
                UrlStep urlStep = new UrlStep(project, connection, wizardModel);
                steps.add(urlStep);
                steps.add(new DetailsStep(project, connection, wizardModel));
                wizardModel.addSteps(steps);
                EditTemplateSourceWizard wizard = new EditTemplateSourceWizard(project, steps);
                urlStep.addListener(wizard);
                boolean rc = wizard.showAndGet();
                if (rc) {
                    RepoEntry newRepoEntry = wizard.getRepoEntry();
                    if (newRepoEntry != null) {
                        newRepoEntry.enabled = selectedRepo.enabled;
                        repoEntries.set(selectedIndex, newRepoEntry);
                        repoViewerModel.fireTableDataChanged();
                        repoViewer.setRowSelectionInterval(selectedIndex, selectedIndex);
                    }
                }
            }
        });

        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int[] selectedIndices = repoViewer.getSelectedRows();
                if (selectedIndices.length > 0) {
                    List<RepoEntry> itemsToRemove = new ArrayList<>();
                    for (int i = 0; i < selectedIndices.length; i++) {
                        itemsToRemove.add(repoEntries.get(selectedIndices[i]));
                    }
                    Arrays.stream(itemsToRemove.toArray()).forEach(item -> {
                        repoEntries.remove(item);
                    });
                    repoViewerModel.fireTableDataChanged();
                }
            }
        });
    }

    private void updateDetails() {
        String desc = "";
        String styles = "";
        String url = "";
        boolean enabled = false;
        if (repoViewer.getSelectedRowCount() == 1) {
            RepoEntry repoEntry = repoEntries.get(repoViewer.getSelectedRow());
            enabled = repoEntry != null;
            if (enabled) {
                desc = repoEntry.description;
                styles = repoEntry.getStyles();
                url = repoEntry.url;
            }
        }
        descLabel.setEnabled(enabled);
        descTextPane.setText(desc);
        styleLabel.setEnabled(enabled);
        styleTextPane.setText(styles);
        urlLabel.setEnabled(enabled);
        if (url.length() == 0) {
            urlTextPane.setContentType("text/string"); // Not NLS
            urlTextPane.setText(url);
        } else {
            urlTextPane.setContentType("text/html"); // Not NLS
            urlTextPane.setText("<a href=\"" + url + "\">" + url + "</a>");  // Not NLS
        }
    }

    private void updateButtons() {
        boolean enabled = true;
        if (repoViewer.getSelectedRowCount() > 0) {
            int[] selectedRows = repoViewer.getSelectedRows();
            for (int i = 0; i < selectedRows.length; i++) {
                RepoEntry repoEntry = repoEntries.get(selectedRows[i]);
                if (repoEntry.isProtected()) {
                    enabled = false;
                    break;
                }
            }
        } else {
            enabled = false;
        }
        removeButton.setEnabled(enabled);
        editButton.setEnabled(repoViewer.getSelectedRowCount() == 1 && enabled);
    }


    public ValidationInfo doValidate() {
        return null;
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    private List<RepoEntry> getRepoEntries(List<RepositoryInfo> infos) {
        if (infos == null) return new ArrayList<>(0);
        List<RepoEntry> entries = new ArrayList<>(infos.size());
        for (RepositoryInfo info : infos) {
            entries.add(new RepoEntry(info));
        }
        return entries;
    }

    // This should only be called once the user has made all of their changes
    // and indicated they want to update (clicked OK rather than Cancel).
    public void updateRepos() {

        if (!hasChanges()) {
            return;
        }
        ApplicationManager.getApplication().invokeLater(() -> {
            ProgressManager.getInstance().run(new Task.Backgroundable(project, message("RepoUpdateTask"), true) {
                @Override
                public void run(@NotNull ProgressIndicator mon) {
                    // Check for the differences between the original repo set and the new set
                    for (RepositoryInfo info : repoList) {
                        Optional<RepoEntry> entry = getRepoEntry(info);
                        if (!entry.isPresent()) {
                            // The new set does not contain the original repo so remove it
                            try {
                                TemplateUtil.removeTemplateSource(info.getURL(), connection.getConid(), mon);
                            } catch (Exception e) {
                                Logger.logWarning("Failed to remove repository: " + info.getURL(), e); // Not NLS
                            }
                        } else if (info.getEnabled() != entry.get().enabled) {
                            // The new set contains the original repo but the enablement does not match so update it
                            try {
                                TemplateUtil.enableTemplateSource(entry.get().enabled, info.getURL(), connection.getConid(), mon);
                            } catch (Exception e) {
                                Logger.logWarning("Failed to update repository: " + info.getURL(), e); // Not NLS
                            }
                        }
                        if (mon.isCanceled()) {
                            return;
                        }
                    }
                    // Check for new entries (RepositoryInfo is null) and add them
                    for (RepoEntry entry : repoEntries) {
                        if (entry.info == null) {
                            // Add the repository
                            try {
                                TemplateUtil.addTemplateSource(entry.url, entry.username, entry.password, entry.accessToken, entry.name, entry.description, connection.getConid(), mon);
                            } catch (Exception e) {
                                Logger.logWarning("Failed to add repository: " + entry.url, e); // Not NLS
                                CoreUtil.openDialog(true, message("RepoMgmtAddFailed", entry.url), e.getLocalizedMessage());
                            }
                        }
                        if (mon.isCanceled()) {
                            return;
                        }
                    }
                }
            });
        });
    }

    public boolean hasChanges() {
        // For all pre-existing repos, if not in the current list then they
        // need to be removed. If in the current list but the enablement is
        // different then they need to be updated.
        for (RepositoryInfo info : repoList) {
            Optional<RepoEntry> entry = getRepoEntry(info);
            if (!entry.isPresent()) {
                return true;
            } else if (info.getEnabled() != entry.get().enabled) {
                return true;
            }
        }
        // For all entries that are not pre-existing (RepositoryInfo is null),
        // they need to be added.
        for (RepoEntry entry : repoEntries) {
            if (entry.info == null) {
                return true;
            }
        }
        return false;
    }

    private Optional<RepoEntry> getRepoEntry(RepositoryInfo info) {
        return repoEntries.stream().filter(entry -> entry.info == info).findFirst();
    }



    private class RepoViewerModel extends AbstractTableModel {
        private List<RepoEntry> repoEntries;

        public RepoViewerModel() {
            super();
        }

        public void setRepoEntries(List<RepoEntry> repoEntries) {
            this.repoEntries = repoEntries;
        }

        @Override
        public int getRowCount() {
            if (repoEntries != null) {
                return repoEntries.size();
            }
            return 0;
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Class getColumnClass(int column) {
            switch (column) {
                case 0:
                    return Boolean.class;
                default:
                    return String.class;
            }
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            if (col == 0) {
                return true;
            }
            return false;
        }

        @Override
        public void setValueAt(Object o, int row, int col) {
            super.setValueAt(o, row, col);
            if (o instanceof Boolean) {
                RepoEntry repoEntry = repoEntries.get(row);
                repoEntry.enabled = ((Boolean) o).booleanValue();
            }
            fireTableCellUpdated(row, col);
        }

        @Override
        public Object getValueAt(int row, int col) {
            RepoEntry repoEntry = repoEntries.get(row);
            if (col ==0) {
                return repoEntry.enabled;
            }
            if (col == 1) {
                String name = repoEntry.name;
                if (name == null || name.isEmpty()) {
                    name = repoEntry.description;
                }
                return name;
            }
            return null;
        }
    }

    private class RepoViewer extends JTable {
        private List<ListSelectionListener> selectionListeners = new ArrayList<>();

        public RepoViewer() {
            super();
        }

        public void setRepoEntries(List<RepoEntry> repoEntries) {
            repoViewerModel.setRepoEntries(repoEntries);
        }

        /**
         * Call init() after instantiation
         */
        public void init() {
            setFillsViewportHeight(true);

            setShowGrid(true);
            setColumnSelectionAllowed(false);
            setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);

            JTableHeader tableHeader = new JTableHeader();
            tableHeader.setTable(this);
            tableHeader.setReorderingAllowed(false);
            setTableHeader(tableHeader);

            DefaultTableColumnModel columnModel = new DefaultTableColumnModel();

            TableColumn checkboxColumn = new TableColumn();
            checkboxColumn.setPreferredWidth(50);
            checkboxColumn.setMaxWidth(100);
            checkboxColumn.setModelIndex(0);
            checkboxColumn.setHeaderValue(null);
            columnModel.addColumn(checkboxColumn);

            TableColumn nameColumn = new TableColumn();
            nameColumn.setModelIndex(1);
            nameColumn.setHeaderValue(null);
            columnModel.addColumn(nameColumn);

            repoViewerModel = new RepoViewerModel();
            setModel(repoViewerModel);
//            repoViewerModel.addTableModelListener(new TableModelListener() {
//                @Override
//                public void tableChanged(TableModelEvent event) {
//                    Object source = event.getSource();
//
//                    int row = event.getFirstRow();
//                    int column = event.getColumn();
//                    if (source instanceof RepoViewerModel) {
//                        RepoViewerModel model = (RepoViewerModel)source;
//                    }
//                }
//            });

            setColumnModel(columnModel);
            setTableHeader(null);
            setRowHeight(getRowHeight() + 5);

            ListSelectionModel selectionModel = getSelectionModel();
            selectionModel.addListSelectionListener(listSelectionEvent -> {
                updateDetails();
                updateButtons();
                if (selectionListeners.size() > 0) {
                    for (ListSelectionListener listener : selectionListeners) {
                        listener.valueChanged(listSelectionEvent);
                    }
                }
            });
        }
    }
}
