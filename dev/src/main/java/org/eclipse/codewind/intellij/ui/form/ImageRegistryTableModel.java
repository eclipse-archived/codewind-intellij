package org.eclipse.codewind.intellij.ui.form;

import org.eclipse.codewind.intellij.core.connection.ImagePushRegistryInfo;
import org.eclipse.codewind.intellij.core.connection.RegistryInfo;
import org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle;
import org.eclipse.codewind.intellij.ui.wizard.RegistryTableEntry;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class ImageRegistryTableModel extends AbstractTableModel {

    private List<RegistryInfo> regList;
    private ImagePushRegistryInfo pushReg;  // The actual push registry at the time the dialog is invoked
    private RegistryInfo selectedPushReg;  // The currently selected push registry

    private List<RegistryTableEntry> regEntries = new ArrayList<>();

    public ImageRegistryTableModel(List<RegistryInfo> regList, ImagePushRegistryInfo pushReg) {
        this.regList = regList;
        this.pushReg = pushReg;
        regEntries = getRegEntries(regList, pushReg);
    }

    public List<RegistryTableEntry> getRegEntries() {
        return regEntries;
    }

    public void setAsPushRegistry(int index) {
        RegistryTableEntry selectedRegistry = regEntries.get(index);
        if (selectedRegistry.namespace == null || selectedRegistry.namespace.length() == 0) {
            selectedRegistry.namespace = "customNS";
        } else {
            selectedRegistry.namespace = selectedRegistry.namespace + "1";
        }
        regEntries.stream().forEach(entry -> { entry.isPushReg = entry.address.equals(selectedRegistry.address); });
    }

    public void addImageRegistry(RegistryTableEntry newEntry) {
        if (newEntry.isPushReg) {
            regEntries.stream().forEach(entry -> { entry.isPushReg = false; });
        }
        regEntries.add(newEntry);
    }

//    public void updatePushRegistrySelection(RegistryInfo regInfo, String namespace) {
//        if (namespaceMap.containsKey(regInfo)) {
//            namespaceMap.remove(regInfo);
//        }
//        namespaceMap.put(regInfo, namespace);
//        selectedPushReg = regInfo;
//    }
//
//    public String getNamespace(RegistryInfo registryInfo) {
//        if (namespaceMap.containsKey(registryInfo)) {
//            return namespaceMap.get(registryInfo);
//        }
//        return null;
//    }

    private String[] columnNames = {"Address",
            "Username",
            "Namespace",
            "Push Registry"};
    private Object[][] data = {
//            {"docker.io", "keithchong", "keithchong", false},
//            {"docker-registry.default.svc:5000", "keithchong", "keithchong", true},
//            {"https://quay.io", "keithchong", "keithchong", false}
    };

    public java.lang.String getColumnName(int column) {

        return columnNames[column];
    }

    public int getColumnCount() {
        return 4;
    }

    public int getRowCount() {
//        return data.length;
        if (regEntries != null) {
            return regEntries.size();
//            return 25;
        }
        return 0;
    }

    // Do not return null
    public Object getValueAt(int row, int col) {
        RegistryTableEntry info = regEntries.get(row);  // row
        switch (col) {
            case 0:
                return info.address;
            case 1:
                return info.username;
            case 2:
                if (info.namespace != null) {
                    return info.namespace;
                }
            case 3:
                if (info.isPushReg) {
                    return CodewindUIBundle.message("RegMgmtPushRegSet");
                }
            default:
        }
        return "";
    }

    /*
     * JTable uses this method to determine the default renderer/
     * editor for each cell.  If we didn't implement this method,
     * then the last column would contain text ("true"/"false"),
     * rather than a check box.
     */
    public Class getColumnClass(int c) {
        Object valueAt = getValueAt(0, c);
        if (valueAt != null) {
            return valueAt.getClass();
        }
        return null;
    }

    /*
     * Don't need to implement this method unless your table's
     * editable.
     */
    public boolean isCellEditable(int row, int col) {
        if (col < 3) {
            return false;
        } else {
            return true;
        }
    }

    /*
     * Don't need to implement this method unless your table's
     * data can change.
     */
    public void setValueAt(Object value, int row, int col) {
//        data[row][col] = value;
        fireTableCellUpdated(row, col);
    }









    private List<RegistryTableEntry> getRegEntries(List<RegistryInfo> infos, ImagePushRegistryInfo pushReg) {
        List<RegistryTableEntry> entries = new ArrayList<>(infos.size());
        for (RegistryInfo info : infos) {
            RegistryTableEntry entry = new RegistryTableEntry(info);
            if (pushReg != null && pushReg.getAddress().equals(info.getAddress())) {
                entry.isPushReg = true;
                entry.namespace = pushReg.getNamespace();
            }
            entries.add(entry);
        }
        return entries;
    }


}
