package org.eclipse.codewind.intellij.ui.form;

import org.eclipse.codewind.intellij.core.connection.ImagePushRegistryInfo;
import org.eclipse.codewind.intellij.core.connection.RegistryInfo;

import javax.swing.ButtonGroup;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableColumn;
import java.util.ArrayList;
import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ImageRegistryTable extends JTable {

    private List<RegistryInfo> regList;
    private ImagePushRegistryInfo pushReg;
    private List<ListSelectionListener> selectionListeners = new ArrayList<>();

    public ImageRegistryTable() {
        super();
    }

    public void init() {
        setFillsViewportHeight(true);

        setShowGrid(true);
        setColumnSelectionAllowed(false);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
//      setGridColor(registriesTable.getGridColor().darker());
//      setGridColor(Color.black);

        JTableHeader tableHeader = new JTableHeader();
        tableHeader.setTable(this);
        tableHeader.setReorderingAllowed(false);
//        tableHeader.setResizingAllowed(true);

        setTableHeader(tableHeader);

        DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
        TableColumn addressColumn = new TableColumn();
        addressColumn.setHeaderValue(new String(message("RegMgmtAddressColumn")));
        addressColumn.setPreferredWidth(300);
//        addressColumn.setResizable(true);
        addressColumn.setModelIndex(0);
        columnModel.addColumn(addressColumn);


        TableColumn usernameColumn = new TableColumn();
        usernameColumn.setHeaderValue(new String(message("RegMgmtUsernameColumn")));
        usernameColumn.setPreferredWidth(100);
        usernameColumn.setModelIndex(1);
//        usernameColumn.setResizable(true);
        columnModel.addColumn(usernameColumn);

        TableColumn namespaceColumn = new TableColumn();
        namespaceColumn.setHeaderValue(new String(message("RegMgmtNamespaceColumn")));
        namespaceColumn.setPreferredWidth(100);
        namespaceColumn.setModelIndex(2);
//        namespaceColumn.setResizable(true);
        columnModel.addColumn(namespaceColumn);

        TableColumn pushRegistryColumn = new TableColumn();
        pushRegistryColumn.setHeaderValue(new String(message("RegMgmtPushRegColumn")));
        pushRegistryColumn.setPreferredWidth(100);
        pushRegistryColumn.setModelIndex(3);
        columnModel.addColumn(pushRegistryColumn);
        ButtonGroup buttonGroup = new ButtonGroup();
//        pushRegisryColumn.setCellRenderer(radioButtonCellEditorRenderer);
//        pushRegisryColumn.setCellEditor(radioButtonCellEditorRenderer);
//        pushRegisryColumn.setCellRenderer(new DefaultTableCellRenderer() {
//            @Override
//            public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
//                Component c = super.getTableCellRendererComponent(jTable, value,
//                        isSelected, hasFocus, row, col);
//                System.out.println("4th column " + value + ", " + isSelected + " , " + hasFocus);
//                return c;
//            }
//        });


        setModel(new ImageRegistryTableModel(regList, pushReg));

        setColumnModel(columnModel);


        ListSelectionModel selectionModel = getSelectionModel();
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent listSelectionEvent) {
                System.out.println("**** Changed " + listSelectionEvent.getSource());
                System.out.println("**** Selected row " + getSelectedRow());
                if (selectionListeners.size() > 0) {
                    for (ListSelectionListener listener : selectionListeners) {
                        listener.valueChanged(listSelectionEvent);
                    }
                }
            }
        });

        int columnCount = getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            TableColumn column = getColumnModel().getColumn(i);
            System.out.println("***** " + i + " = " + column.getPreferredWidth());
        }

        System.out.println("Table pref sized= " + getPreferredSize());
//        TableCellRenderer defaultRenderer = getDefaultRenderer(Boolean.class);
//        setDefaultRenderer(String.class, new DefaultTableCellRenderer() {
//
//            @Override
//            public Component getTableCellRendererComponent(JTable jTable, Object value, boolean isSelected, boolean hasFocus, int row, int col) {
//                Component c = super.getTableCellRendererComponent(jTable, value,
//                        isSelected, hasFocus, row, col);
//                System.out.println("String renderer: "  + c.getClass() + " , " + value + ", " + isSelected + " , " + hasFocus + " , " + row +  ", " + col);
//
//                return c;
//            }
//
//        });

//        setCellEditor(radioButtonCellEditorRenderer);
    }

    public void addSelectionListener(ListSelectionListener listener) {
        this.selectionListeners.add(listener);
    }


    public void setImageRegistryList(List<RegistryInfo> regList, ImagePushRegistryInfo pushReg) {
        this.regList = regList;
        this.pushReg = pushReg;
    }
}
