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
package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.CodewindObjectFactory;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.cli.AuthUtil;
import org.eclipse.codewind.intellij.core.cli.ConnectionUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.eclipse.codewind.intellij.ui.form.RemoteConnectionForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.net.URI;
import java.util.Arrays;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class NewCodewindConnectionStep extends AbstractConnectionWizardStep {
    public static String STEP_ID = "NewConnectionStep";
    private CodewindConnection connection;
    private ConnectionManager connectionManager;

    public NewCodewindConnectionStep(@Nullable String title, Project project, ConnectionManager connectionManager) {
        super(title);
        this.project = project;
        this.connectionManager = connectionManager;
    }

    @NotNull
    @Override
    public Object getStepId() {
        return STEP_ID;
    }

    @Override
    public JComponent getComponent() {
        if (form != null) {
            return form.getContentPane();
        }
        form = new RemoteConnectionForm(this, this.project, this.connectionManager, null);
        form.setDescriptionTextArea(message("NewConnectionPage_WizardDescription"));
        return form.getContentPane();
    }

    public void doOKAction() {
        String name = form.getConnectionName();
        Task.Backgroundable task = new Task.Backgroundable(project, message("NewConnectionWizard_CreateJobTitle", form.getConnectionName()), true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String errorMessage = createConnection();
            }
        };
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        ProgressManager.getInstance().run(task);
    }

    private String createConnection() {
        String name = form.getConnectionName();
        String errorMessage = null;
        connection = null;
        String user = form.getUsername();
        System.out.println("***** Creating connection for " + name + ", " + user);
        try {
            String url = form.getUrl();
            URI uri = new URI(url);
            String pass = form.getPassword();
            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
            String conid = ConnectionUtil.addConnection(name, uri.toString(), user, indicator);
            if (indicator.isCanceled()) {
                removeConnection(conid, indicator);
                return "Cancelled";
            }
            // if conid is still null return??
            connection = CodewindObjectFactory.createRemoteConnection(name, uri, conid, user, null);
            AuthToken token = AuthUtil.genAuthToken(user, pass, conid, indicator);
            if (indicator.isCanceled()) {
                removeConnection(indicator);
                return "Cancelled";
            }
            connection.setAuthToken(token);
            connection.connect();
            if (indicator.isCanceled()) {
                removeConnection(indicator);
                return "Cancelled";
            }
        } catch (Exception e) {
            String msg;
            if (connection == null) {
                msg = message("CodewindConnectionCreateError", name);
            } else {
                msg = message("CodewindConnectionConnectError", name);
            }
            Throwable unwrap = Logger.unwrap(e);
            StackTraceElement[] stackTrace = unwrap.getStackTrace();
            String s = Arrays.toString(stackTrace);
            CoreUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Messages.showErrorDialog( msg + "\n\n" + e.getLocalizedMessage() + "\n\n" + s, "Connection Error");
                }
            });
            return msg;
        } finally {
            if (connection != null) {
                ConnectionManager.add(connection);
                CoreUtil.updateAll();
//                ViewHelper.openCodewindExplorerView();
//                CodewindUIPlugin.getUpdateHandler().updateAll();
            }
        }
        return null;
    }

    private void removeConnection(ProgressIndicator indicator) {
        if (connection == null) {
            return;
        }
        connection.disconnect();
        removeConnection(connection.getConid(), indicator);
        connection = null;
    }

    private void removeConnection(String conid, ProgressIndicator indicator) {
        if (conid == null) {
            return;
        }
        try {
            ConnectionUtil.removeConnection(conid, indicator);
        } catch (Exception e) {
            Logger.logWarning("An error occurred trying to de-register connection: " + conid, e);
        }
    }

}
