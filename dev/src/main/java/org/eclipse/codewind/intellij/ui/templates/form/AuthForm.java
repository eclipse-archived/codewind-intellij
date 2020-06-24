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

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.ThrowableComputable;
import com.intellij.ui.DocumentAdapter;
import com.intellij.ui.components.JBPasswordField;
import com.intellij.ui.components.JBRadioButton;
import com.intellij.ui.components.JBTextField;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.HttpUtil;
import org.eclipse.codewind.intellij.core.IAuthInfo;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.WidgetUtils;
import org.eclipse.codewind.intellij.ui.form.WizardHeaderForm;
import org.eclipse.codewind.intellij.ui.wizard.BaseCodewindForm;
import org.jetbrains.annotations.NotNull;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.DocumentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.Base64;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class AuthForm extends BaseCodewindForm {
    private JPanel contentPane;
    private JPanel authenticationMethodPanel;
    private JRadioButton logonRadioButton;
    private JPanel logonPanel;
    private JRadioButton tokenRadioButton;
    private JPanel tokenPanel;
    private JLabel usernameLabel;
    private JTextField usernameTextField;
    private JLabel passwordLabel;
    private JPasswordField passwordField;
    private JLabel tokenLabel;
    private JPanel headerPanel;
    private JPanel mainContent;
    private JPanel buttonRow;
    private JButton testButton;
    private JPasswordField tokenField;

    private CodewindConnection connection;
    private Project project;
    private String urlValue;
    private boolean isLogonMethod;
    private String usernameValue, passwordValue, tokenValue;
    private boolean showHeader;

    public static String AUTH_FAILED = "AUTH_FAILED"; // not NLS
    public static String AUTH_SUCCESSFUL = "AUTH_SUCCESSFUL"; // not NLS

    public AuthForm(Project project, CodewindConnection connection, boolean isLogonMethod, boolean showHeader) {
        this.project = project;
        this.connection = connection;
        this.isLogonMethod = isLogonMethod;
        this.showHeader = showHeader;
    }

    private void createUIComponents() {
        WizardHeaderForm headerForm = new WizardHeaderForm(message("AddRepoDialogAuthentication"), message("AddRepoAuthPageMessage"));
        headerPanel = headerForm.getContentPane();
        headerPanel.setVisible(showHeader);

        authenticationMethodPanel = new JPanel();
        authenticationMethodPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "Authentication method"));

        logonRadioButton = new JBRadioButton();
        logonRadioButton.setText(message("AddRepoDialogLogonAuthButton"));

        tokenRadioButton = new JBRadioButton();
        tokenRadioButton.setText(message("AddRepoDialogAccessTokenAuthButton"));

        usernameTextField = new JBTextField();
        passwordField = new JBPasswordField();
        tokenField = new JBPasswordField();

        usernameTextField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateAuthValues();
            }
        });

        passwordField.getDocument().addDocumentListener(new DocumentAdapter() {
            @Override
            protected void textChanged(@NotNull DocumentEvent e) {
                updateAuthValues();
            }
        });

        tokenField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent keyEvent) {
                updateAuthValues();
            }
        });

        ButtonGroup buttonGroup =  new ButtonGroup();
        buttonGroup.add(logonRadioButton);
        buttonGroup.add(tokenRadioButton);

        if (isLogonMethod) {
            logonRadioButton.setSelected(true);
            tokenField.setEnabled(false);
            usernameTextField.setEnabled(true);
            passwordField.setEnabled(true);

        } else {
            tokenRadioButton.setSelected(true);
            tokenField.setEnabled(true);
            usernameTextField.setEnabled(false);
            passwordField.setEnabled(false);
        }

        logonRadioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                isLogonMethod = true;
                tokenField.setEnabled(false);
                usernameTextField.setEnabled(true);
                passwordField.setEnabled(true);
            }
        });

        tokenRadioButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                isLogonMethod = false;
                tokenField.setEnabled(true);
                usernameTextField.setEnabled(false);
                passwordField.setEnabled(false);
            }
        });

        testButton = new JButton();
        testButton.setText(message("AddRepoDialogTestButtonLabel"));
        testButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent mouseEvent) {
                try {
                    ThrowableComputable<Object, Exception> computable = new ThrowableComputable() {
                        @Override
                        public Object compute() throws Exception {
                            HttpUtil.HttpResult result = null;
                            try {
                                result = HttpUtil.get(new URI(urlValue), getAuthInfo());
                                if (!result.isGoodResponse) {
                                    String errorMsg = result.error;
                                    if (errorMsg == null || errorMsg.trim().isEmpty()) {
                                        errorMsg = message("AddRepoDialogTestFailedDefaultMsg", result.responseCode);
                                    }
                                    throw new InvocationTargetException(new IOException(errorMsg));
                                }
                            } catch (IOException e) {
                                throw new InvocationTargetException(e, e.toString());
                            }
                            return result;
                        }
                    };
                    ProgressManager.getInstance().runProcessWithProgressSynchronously(computable, message("AddRepoDialogAuthTestTaskLabel", urlValue), true, project);
                    CoreUtil.openDialog(false, message("AddRepoDialogAuthTestFailedTitle"), message("AddRepoDialogTestSuccessMsg"));
                    fireStateChanged(new ChangeEvent(AUTH_SUCCESSFUL));
                } catch (Exception e) {
                    String msg = e instanceof InvocationTargetException ? e.getCause().toString() : e.toString();
                    CoreUtil.openDialog(true, message("AddRepoDialogTestFailedTitle"), message("AddRepoDialogAuthTestFailedError", urlValue, msg));
                    fireStateChanged(new ChangeEvent(AUTH_FAILED));
                    return;
                }
            }
        });

    }

    public IAuthInfo getAuthInfo() {
        return isLogonMethod ? new LogonAuth(usernameValue, passwordValue) : new TokenAuth(tokenValue);
    }

    public JComponent getContentPane() {
        return this.contentPane;
    }

    public String getUsername() {
        if (logonRadioButton.isSelected()) {
            return usernameTextField.getText();
        }
        return null;
    }

    public String getPassword() {
        if (logonRadioButton.isSelected()) {
            return WidgetUtils.getPassword(passwordField);
        }
        return null;
    }

    public String getToken() {
        if (tokenRadioButton.isSelected()) {
            return WidgetUtils.getPassword(tokenField);
        }
        return null;
    }

    public void setUrlValue(String url) {
        this.urlValue = url;
    }

    public void setEnabled(boolean isEnabled) {
        authenticationMethodPanel.setEnabled(isEnabled);
        buttonRow.setEnabled(isEnabled);
        contentPane.setEnabled(isEnabled);
        logonRadioButton.setEnabled(isEnabled);
        tokenRadioButton.setEnabled(isEnabled);
        usernameLabel.setEnabled(isEnabled);
        passwordLabel.setEnabled(isEnabled);
        tokenLabel.setEnabled(isEnabled);
        usernameTextField.setEnabled(isEnabled && logonRadioButton.isSelected());
        passwordField.setEnabled(isEnabled && logonRadioButton.isSelected());
        tokenField.setEnabled(isEnabled && tokenRadioButton.isSelected());
    }

    private void updateAuthValues() {
        isLogonMethod = logonRadioButton.isSelected();
        if (isLogonMethod) {
            usernameValue = WidgetUtils.getTextValue(usernameTextField);
            passwordValue = WidgetUtils.getTextValue(passwordField);
        } else {
            tokenValue = WidgetUtils.getTextValue(tokenField);
        }
    }

    public static class LogonAuth implements IAuthInfo {

        private final String username;
        private final String password;

        public LogonAuth(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        public boolean isValid() {
            return username != null && password != null;
        }

        @Override
        public String getHttpAuthorization() {
            try {
                String auth = username + ":" + password;
                return "Basic " + Base64.getEncoder().encodeToString(auth.getBytes("UTF-8"));  // NON NLS
            } catch (UnsupportedEncodingException e) {
                Logger.logWarning("An unsupported encoding exception occurred trying to encode the logon authentication."); // NON NLS
            }
            return null;
        }
    }

    public static class TokenAuth implements IAuthInfo {

        private final String token;

        public TokenAuth(String token) {
            this.token = token;
        }

        @Override
        public boolean isValid() {
            return token != null;
        }

        @Override
        public String getHttpAuthorization() {
            return "bearer " + token;
        }  // NON NLS
    }
}
