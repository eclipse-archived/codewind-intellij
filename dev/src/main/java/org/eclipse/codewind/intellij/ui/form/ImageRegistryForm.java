package org.eclipse.codewind.intellij.ui.form;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.ProgressManagerImpl;
import com.intellij.openapi.progress.util.ProgressIndicatorUtils;
import com.intellij.openapi.progress.util.ProgressWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBScrollPane;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.cli.RegistryUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ImagePushRegistryInfo;
import org.eclipse.codewind.intellij.core.connection.RegistryInfo;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.eclipse.codewind.intellij.ui.wizard.RegistryTableEntry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import java.awt.Dimension;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ImageRegistryForm implements ListSelectionListener {
    private JTextPane descriptionTextPane;
    private JPanel contentPane;
    private JTable registriesTable;
    private JButton addButton;
    private JButton setAsPushButton;
    private JButton removeButton;
    private JPanel rowPanel;
    private JPanel buttonPanel;
    private JScrollPane tableScrollPane;

    private CodewindConnection connection;
    private Project project;
    private List<RegistryInfo> regList;
    private ImagePushRegistryInfo pushReg;

    private boolean supportsPushRegistry;

    public ImageRegistryForm(Project project, CodewindConnection connection) {
        super();
        this.project = project;
        this.connection = connection;
        supportsPushRegistry = !connection.isLocal();
    }

    private void createUIComponents() {
        descriptionTextPane = new JTextPane();
        registriesTable = new ImageRegistryTable();
        tableScrollPane = new JBScrollPane(registriesTable);
        tableScrollPane.setPreferredSize(new Dimension(600, 200));
//        tableScrollPane.setBorder(new BevelBorder(BevelBorder.RAISED));
//        tableScrollPane.setBorder(new LineBorder(Color.BLACK, 2));
//        registriesTable.setFillsViewportHeight(true);
//        tableScrollPane.add(registriesTable);
        addButton = new JButton();
        setAsPushButton = new JButton();
        removeButton = new JButton();

//        descriptionTextPane.setForeground(errorLabel.getForeground());
//        descriptionTextPane.setBackground(errorLabel.getBackground());

        ((ImageRegistryTable) registriesTable).addSelectionListener(this);

        addButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);

                AddRegistryDialogWrapper dialogWrapper = new AddRegistryDialogWrapper(project);
                dialogWrapper.setRegistryList(getRegList());
                boolean b = dialogWrapper.showAndGet();
                if (b) {
                    RegistryTableEntry newRegEntry = dialogWrapper.getNewRegEntry();
                    ImageRegistryTableModel model = (ImageRegistryTableModel) registriesTable.getModel();
                    model.addImageRegistry(newRegEntry);
                    model.fireTableDataChanged();
                }

//                List<AbstractWizardStepEx> steps = new ArrayList<>();
//                AddImageRegistryStep step = new AddImageRegistryStep("Add", project, connection);
//                steps.add(step);
//                AddImageRegistryWizard wizard = new AddImageRegistryWizard(message("RegMgmtAddDialogTitle"), project, steps);
//                boolean rc = wizard.showAndGet();

            }
        });

        setAsPushButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = registriesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    ImageRegistryTableModel model = (ImageRegistryTableModel) registriesTable.getModel();
                    List<RegistryTableEntry> regEntries = model.getRegEntries();
                    RegistryTableEntry selectedRegistry = regEntries.get(selectedRow);
                    if (selectedRegistry.namespace == null || selectedRegistry.namespace.length() == 0) {
                        NamespaceDialogWrapper dialogWrapper = new NamespaceDialogWrapper(project);
                        boolean b = dialogWrapper.showAndGet();
                        if (b) {
                            selectedRegistry.namespace = dialogWrapper.getNamespace();
                        } else {
                            return;
                        }
                    }
                    regEntries.stream().forEach(entry -> { entry.isPushReg = entry.address.equals(selectedRegistry.address); });

//                    model.setAsPushRegistry(selectedRow);
                    model.fireTableDataChanged();
                }
            }
        });

        removeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                super.mouseClicked(e);
                int selectedRow = registriesTable.getSelectedRow();
                if (selectedRow >= 0) {
                    ImageRegistryTableModel model = (ImageRegistryTableModel) registriesTable.getModel();
                    List<RegistryTableEntry> regEntries = model.getRegEntries();
                    RegistryTableEntry selectedRegistry = regEntries.get(selectedRow);
                    regEntries.remove(selectedRegistry);
                    model.fireTableDataChanged();
                }
            }
        });

//        registriesTable.setShowGrid(true);
//        registriesTable.setColumnSelectionAllowed(false);
//        registriesTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
////        registriesTable.setGridColor(registriesTable.getGridColor().darker());
//        //registriesTable.setGridColor(Color.black);

//        JTableHeader tableHeader = new JTableHeader();
//        tableHeader.setTable(registriesTable);
//        tableHeader.setReorderingAllowed(false);
//        tableHeader.setResizingAllowed(true);
//
//        registriesTable.setTableHeader(tableHeader);

//        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
//        TableColumn addressColumn = new TableColumn();
//        addressColumn.setHeaderValue(new String(message("RegMgmtAddressColumn")));
//        addressColumn.setPreferredWidth(5000);
//        addressColumn.setResizable(true);
//        addressColumn.setModelIndex(0);
//        columnModel.addColumn(addressColumn);
//
//
//        TableColumn usernameColumn = new TableColumn();
//        usernameColumn.setHeaderValue(new String(message("RegMgmtUsernameColumn")));
//        usernameColumn.setPreferredWidth(700);
//        usernameColumn.setModelIndex(1);
//        usernameColumn.setResizable(true);
//        columnModel.addColumn(usernameColumn);
//
//        TableColumn namespaceColumn = new TableColumn();
//        namespaceColumn.setHeaderValue(new String(message("RegMgmtNamespaceColumn")));
//        namespaceColumn.setPreferredWidth(700);
//        namespaceColumn.setModelIndex(2);
//        namespaceColumn.setResizable(true);
//        columnModel.addColumn(namespaceColumn);
//
//        TableColumn pushRegisryColumn = new TableColumn();
//        pushRegisryColumn.setHeaderValue(new String(message("RegMgmtPushRegColumn")));
//        pushRegisryColumn.setPreferredWidth(700);
//        pushRegisryColumn.setModelIndex(3);
//        columnModel.addColumn(pushRegisryColumn);
//        registriesTable.setColumnModel(columnModel);

//
//        TableCellRenderer cellRenderer = new ColoredTableCellRenderer() {
//            @Override
//            protected void customizeCellRenderer(JTable table, @Nullable Object value, boolean selected, boolean hasFocus, int row, int column) {
//                setBackground(Color.LIGHT_GRAY);
//                setOpaque(true);
//                setForeground(Color.black);
//            }
//        };

//        registriesTable.setDefaultRenderer(String.class, new TableCellRenderer() {
//
//            @Override
//            public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
//                System.out.println("getTableCellRendererComponent at " + o + ", " + i + ", " + i1);
//                JLabel label = new JLabel();
//                label.setText(o.toString());
//                return label;
//            }
//        });

//        registriesTable.getTableHeader().setDefaultRenderer(new TableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable jTable, Object o, boolean b, boolean b1, int i, int i1) {
//                System.out.println("header renderer at " + o + ", " + i + ", " + i1);
//
//                JLabel label = new JLabel();
//                label.setText(o.toString());
//                return label;
//            }
//        });

//        registriesTable.setModel(new ImageRegistryTableModel());

        addButton.setText(message("RegMgmtAddButton"));
        setAsPushButton.setText(message("RegMgmtSetPushButton"));
        setAsPushButton.setEnabled(false);
        removeButton.setText(message("RegMgmtRemoveButton"));
    }

    public void update(ProgressIndicator monitor) throws JSONException, TimeoutException, IOException {
        regList = RegistryUtil.listRegistrySecrets(connection.getConid(), monitor);
        pushReg = connection.requestGetPushRegistry();
        ((ImageRegistryTable) registriesTable).setImageRegistryList(regList, pushReg);
        ((ImageRegistryTable) registriesTable).init();
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public boolean isComplete() {
//        return getConnectionName().length() > 0 && getUrl().length() > 0 && getUsername().length() > 0 && passwordField.getPassword().length > 0;
        return true;
    }

    public void setDescriptionText(String text) {
        descriptionTextPane.setText(text);
    }

    public String validatePage() {
        return null;
    }

    @Override
    public void valueChanged(ListSelectionEvent listSelectionEvent) {
        this.setAsPushButton.setEnabled(registriesTable.getSelectedRowCount() > 0);
        this.removeButton.setEnabled(registriesTable.getSelectedRowCount() > 0);

    }

    public boolean hasChanges() {
        ImageRegistryTableModel model = (ImageRegistryTableModel) registriesTable.getModel();
        List<RegistryTableEntry> regEntries = model.getRegEntries();

        for (RegistryInfo info : regList) {
            RegistryTableEntry entry = getRegEntry(info.getAddress(), regEntries);
            if (entry == null) {
                return true;
            }
        }
        for (RegistryTableEntry entry : regEntries) {
            RegistryInfo info = getRegInfo(entry.address);
            if (info == null) {
                return true;
            } else if (entry.isPushReg && (pushReg == null || !pushReg.getAddress().equals(entry.address))) {
                return true;
            }
        }
        return false;
    }

    private RegistryTableEntry getRegEntry(String address, List<RegistryTableEntry> regEntries) {
        for (RegistryTableEntry entry : regEntries) {
            if (address.equals(entry.address)) {
                return entry;
            }
        }
        return null;
    }

    private RegistryInfo getRegInfo(String address) {
        for (RegistryInfo info : regList) {
            if (address.equals(info.getAddress())) {
                return info;
            }
        }
        return null;
    }


    public void doFinish() {
        ImageRegistryTableModel model = (ImageRegistryTableModel) registriesTable.getModel();
        List<RegistryTableEntry> regEntries = model.getRegEntries();
        if (regList != null) {
            regList.size();
        }
        if (pushReg != null) {
            pushReg.getAddress();
        }

        System.out.println("Entries have changes" + hasChanges());

        if (hasChanges()) {
            updateRegistries(regEntries);
        }
    }

    private void updateRegistries(List<RegistryTableEntry> regEntries) {


        ApplicationManager.getApplication().invokeLater(() -> {
                ProgressManager.getInstance().run(new Task.Backgroundable(project, "Updating registries", true) {
                    @Override
                    public void run(@NotNull ProgressIndicator mon) {
                        System.out.println("Mon is " + mon);
                        // Check for the differences between the original registry set and the new set
                        for (RegistryInfo info : regList) {
                            RegistryTableEntry entry = getRegEntry(info.getAddress(), regEntries);
                            if (entry == null) {
                                // Remove the registry
                                try {
                                    if (pushReg != null && pushReg.getAddress().equals(info.getAddress())) {
                                        connection.requestDeletePushRegistry(info.getAddress());
                                    }
                                    if (mon.isCanceled()) {
                                        return;
                                    }
                                    RegistryUtil.removeRegistrySecret(info.getAddress(), connection.getConid(), mon);
                                } catch (Exception e) {
//                            Logger.logError("Failed to remove registry: " + info.getAddress(), e); //$NON-NLS-1$
//                            multiStatus.add(new Status(IStatus.ERROR, CodewindCorePlugin.PLUGIN_ID, NLS.bind(Messages.RegMgmtRemoveFailed, info.getAddress()), e));
                                }
                            }
                            if (mon.isCanceled()) {
                                return;
                            }
//                    mon.setWorkRemaining(100);
                        }
                        for (RegistryTableEntry entry : regEntries) {
                            RegistryInfo info = getRegInfo(entry.address);
                            if (info == null) {
                                // Add the registry
                                try {
                                    RegistryUtil.addRegistrySecret(entry.address, entry.username, entry.password, connection.getConid(), mon);
                                } catch (Exception e) {
//                            Logger.logError("Failed to add registry: " + entry.address, e); //$NON-NLS-1$
//                            multiStatus.add(new Status(IStatus.ERROR, CodewindCorePlugin.PLUGIN_ID, NLS.bind(Messages.RegMgmtAddFailed, entry.address), e));
                                }
                            }
                            if (mon.isCanceled()) {
                                return;
                            }
//                    mon.setWorkRemaining(100);
                            if (entry.isPushReg) {
                                try {
                                    connection.requestSetPushRegistry(entry.address, entry.namespace == null ? "" : entry.namespace);
                                } catch (Exception e) {
//                            Logger.logError("Failed to set the push registry: " + info.getAddress(), e); //$NON-NLS-1$
//                            multiStatus.add(new Status(IStatus.ERROR, CodewindCorePlugin.PLUGIN_ID, NLS.bind(Messages.RegMgmtSetPushRegFailed, info.getAddress()), e));
                                }
                            }
                            if (mon.isCanceled()) {
                                return;
                            }
                            mon.setFraction(1.0);
                        }

                    }
                });
        });

    }


    private List<RegistryInfo> getRegList() {
        return this.regList;
    }

    private class AddRegistryDialogWrapper extends AbstractCodewindDialogWrapper {
        private AddImageRegistryForm form;
        private List<RegistryInfo> regList;

        public AddRegistryDialogWrapper(Project project) {
            super(project, message("RegMgmtAddDialogTitle"), UIConstants.REGISTRY_INFO_URL);
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            if (form == null) {
                form = new AddImageRegistryForm();
            }
            return form.getContentPane();
        }

        public void setRegistryList(List<RegistryInfo> regList) {
            this.regList = regList;
            form.setRegistryList(regList);
        }

        // Override to do custom on the fly widget validation to enable/disable the OK button
        protected ValidationInfo doValidate() {
            return form.doValidate();
        }

        public RegistryTableEntry getNewRegEntry() {
            return form.getNewRegEntry();
        }
    }

    private class NamespaceDialogWrapper extends AbstractCodewindDialogWrapper {

        private PushRegistryNamespaceForm form;

        public NamespaceDialogWrapper(Project project) {
            super(project, message("RegMgmtNamespaceDialogShell"), UIConstants.REGISTRY_INFO_URL);
        }

        @Nullable
        @Override
        protected JComponent createCenterPanel() {
            if (form == null) {
                form = new PushRegistryNamespaceForm();
            }
            return form.getContentPane();
        }

        @Override
        protected ValidationInfo doValidate() {
            return form.doValidate();
        }

        public String getNamespace() {
            return form.getNamespace();
        }
    }
}