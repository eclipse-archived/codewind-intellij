package org.eclipse.codewind.intellij.ui.form;

import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.components.JBTextField;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class PushRegistryNamespaceForm {
    private JPanel contentPane;
    private JTextPane descriptionTextPane;
    private JPanel rowPanel;
    private JLabel namespaceLabel;
    private JTextField namespaceTextField;

    private void createUIComponents() {
        descriptionTextPane = new JTextPane();
        descriptionTextPane.setText(message("RegMgmtNamespaceDialogMessage"));

        namespaceLabel = new JLabel();
        namespaceLabel.setText(message("RegMgmtAddDialogNamespaceLabel"));

        namespaceTextField = new JBTextField();
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public boolean isComplete() {
        return true;
    }

    public ValidationInfo doValidate() {
        // Accept a blank namespace
        return null;
    }

    public String getNamespace() {
        return namespaceTextField.getText();
    }
}