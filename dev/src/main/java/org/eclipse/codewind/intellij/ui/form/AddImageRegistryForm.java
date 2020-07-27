package org.eclipse.codewind.intellij.ui.form;

import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBTextField;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.RegistryInfo;
import org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle;
import org.eclipse.codewind.intellij.ui.wizard.RegistryTableEntry;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AddImageRegistryForm {
    private JPanel contentPane;
    private JTextPane descriptionTextPane;
    private JPanel rowPanel;
    private JLabel addressLabel;
    private JComboBox addressComboBox;
    private JLabel usernameLabel;
    private JTextField usernameField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JCheckBox setAsPushRegistryCheckBox;
    private JLabel namespaceLabel;
    private JTextField namespaceField;

    private CodewindConnection connection;
    private List<RegistryInfo> regList;

    private static final String[] regAddresses = new String[]{"docker.io", "https://quay.io", "docker-registry.default.svc:5000"};


    private void createUIComponents() {
        descriptionTextPane = new JTextPane();
        addressComboBox = new ComboBox();
        usernameField = new JBTextField();
        passwordField = new JBPasswordField();
        setAsPushRegistryCheckBox = new JBCheckBox();
        namespaceLabel = new JLabel();
        namespaceField = new JBTextField();

        descriptionTextPane.setText(message("RegMgmtAddDialogMessage"));

        for (int i = 0; i < regAddresses.length; i++) {
            addressComboBox.addItem(regAddresses[i]);
        }
        addressComboBox.setSelectedItem(null); // Do not select any item initially
        addressComboBox.setEditable(true);
        addressComboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                String actionCommand = actionEvent.getActionCommand();
                System.out.println("Action command is " + actionCommand);
            }
        });

        namespaceField.setVisible(false);
        namespaceLabel.setVisible(false);

        setAsPushRegistryCheckBox.setText(message("RegMgmtAddDialogPushRegLabel"));
        setAsPushRegistryCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                namespaceField.setVisible(setAsPushRegistryCheckBox.isSelected());
                namespaceLabel.setVisible(setAsPushRegistryCheckBox.isSelected());
            }
        });
        addListener(usernameField);
        addListener(passwordField);
        addListener(namespaceField);
    }

    public void setConnection(CodewindConnection connection) {
        this.connection = connection;
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public boolean isComplete() {
        return true;
    }

    private void addListener(JTextField field) {
        field.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
    }

    public ValidationInfo doValidate() {
        ValidationInfo validationInfo = null;
        String item = addressComboBox.getEditor().getItem().toString();
        if (addressComboBox.getSelectedIndex() < 0 && item == null || item.length() == 0) {
            validationInfo = new ValidationInfo(message("RegMgmtAddDialogNoAddress"), addressComboBox);
            return validationInfo;
        } else {
            if (getRegEntry(item) != null) {
                validationInfo = new ValidationInfo(message("RegMgmtAddDialogAddressInUse"), addressComboBox);
                return validationInfo;
            }
        }
        if (usernameField.getText().length() == 0) {
            validationInfo = new ValidationInfo(message("RegMgmtAddDialogNoUsername"), usernameField);
            return validationInfo;
        }
        if (passwordField.getPassword().length == 0) {
            validationInfo = new ValidationInfo(message("RegMgmtAddDialogNoPassword"), passwordField);
            return validationInfo;
        }
        if (setAsPushRegistryCheckBox.isSelected()) {
            if (namespaceField.getText().length() == 0) {
                validationInfo = new ValidationInfo(message("RegMgmtAddDialogNoNamespace"), namespaceField);
                return validationInfo;
            }
        }
        return null;
    }

    public void setRegistryList(List<RegistryInfo> regList) {
        this.regList = regList;
    }

    private RegistryInfo getRegEntry(String address) {
        if (regList == null) return null;
        for (RegistryInfo entry : regList) {
            if (address.equals(entry.getAddress())) {
                return entry;
            }
        }
        return null;
    }

    public RegistryTableEntry getNewRegEntry() {
        String address = addressComboBox.getEditor().getItem().toString();
        System.out.println("***** Address = " + address);
        return new RegistryTableEntry(address, namespaceField.getText(), usernameField.getText(), WidgetUtils.getPassword(passwordField), setAsPushRegistryCheckBox.isSelected());
    }

}