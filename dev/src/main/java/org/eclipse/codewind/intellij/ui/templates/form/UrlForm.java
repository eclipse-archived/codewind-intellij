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

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ValidationInfo;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.ui.components.JBTextField;
import org.eclipse.codewind.intellij.core.IAuthInfo;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.WidgetUtils;
import org.eclipse.codewind.intellij.ui.form.WizardHeaderForm;
import org.eclipse.codewind.intellij.ui.wizard.BaseCodewindForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class UrlForm extends BaseCodewindForm implements ChangeListener {
    private JPanel contentPane;
    private JPanel rowPanel;
    private JLabel urlLabel;
    private JTextField urlTextField;
    private JCheckBox authCheckBox;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JPanel authPanel;
    private AuthForm authForm;

    private CodewindConnection connection;
    private Project project;
    private String urlValue = "";
    private boolean isAuth = false;

    public UrlForm(Project project, CodewindConnection connection, boolean isAuth, String urlValue) {
        this.project = project;
        this.connection = connection;
        this.isAuth = isAuth;
        this.urlValue = urlValue;
    }

    private void createUIComponents() {
        WizardHeaderForm headerForm = new WizardHeaderForm(message("AddRepoDialogUrlLocation"), message("AddRepoURLPageMessage"));
        headerPanel = headerForm.getContentPane();
        urlTextField = new JBTextField();
        authCheckBox = new JBCheckBox();

        authCheckBox.setSelected(isAuth);

        authCheckBox.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent mouseEvent) {
                if (mouseEvent.getSource() == authCheckBox) {
                    if (!mouseEvent.isPopupTrigger()) {
                        isAuth = !isAuth;
                        authForm.setEnabled(isAuth);
                        fireStateChanged(new ChangeEvent(authCheckBox));
                    }
                }
            }
        });

        urlTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                urlValue = WidgetUtils.getTextValue(urlTextField);
                authForm.setUrlValue(urlValue);
                fireStateChanged(new ChangeEvent(urlTextField));
            }
        });

        authPanel = new JPanel();
        authForm = new AuthForm(project, connection, true, false);
        authForm.addListener(this);

        if (urlValue != null) {
            urlTextField.setText(urlValue);
        }

        authForm.setEnabled(isAuth);
    }

    public String getTemplateSourceUrl() {
        return WidgetUtils.getTextValue(urlTextField);
    }

    public JTextField getTemplateSourceTextField() {
        return urlTextField;
    }

    /**
     * Minimal validation to disable/enable the Next button.  Eg. URL cannot be blank.
     * @return
     */
    public ValidationInfo doValidate() {
        return null;
    }

    public boolean isComplete() {
        return urlValue != null ? urlValue.length() > 0 : false;
    }

    public AuthForm getAuthForm() {
        return authForm;
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public IAuthInfo getAuthInfo() {
        if (isAuth) {
            return authForm.getAuthInfo();
        }
        return null; // if auth checkbox is unchecked, return null
    }
    public String getUsername() {
        return authForm.getUsername();
    }

    public String getPassword() {
        return authForm.getPassword();
    }

    public String getToken() {
        return authForm.getToken();
    }

    @Override
    public void stateChanged(ChangeEvent changeEvent) {
        // Empty
    }
}
