package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.CoreUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.cli.AuthUtil;
import org.eclipse.codewind.intellij.core.cli.ConnectionUtil;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.RemoteConnectionForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.net.URI;
import java.util.Arrays;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class EditCodewindConnectionStep extends AbstractConnectionWizardStep {
    public static String STEP_ID = "EditConnectionStep";
    private CodewindConnection connection;

    public EditCodewindConnectionStep(@Nullable String title, Project project, CodewindConnection connection) {
        super(title);
        this.project = project;
        this.connection = connection;
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
        form = new RemoteConnectionForm(this, this.project, null, this.connection);
        form.setDescriptionTextArea(message("EditConnectionDialogMessage", form.getConnectionName()));
        if (this.connection != null) {
            form.initialize();
        }
        return form.getContentPane();
    }

    public void doOKAction() {
        String name = form.getConnectionName();
        Task.Backgroundable task = new Task.Backgroundable(project, message("UpdateConnectionJobLabel", this.connection.getName()), true, PerformInBackgroundOption.DEAF) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String errorMessage = editConnection();
            }
        };
        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        System.out.println("**** indicator is " + progressIndicator);
        ProgressManager.getInstance().run(task);
    }

    private String editConnection() {
        String name = form.getConnectionName();
        String url = form.getUrl();
        String user = form.getUsername();
        if (connection.isConnected()) {
            connection.disconnect();
        }
        ProgressIndicator mon = ProgressManager.getInstance().getProgressIndicator();
        mon.setIndeterminate(false);
        try {
            mon.setFraction(0.2);
            ConnectionUtil.updateConnection(connection.getConid(), name, url, user, mon);
            if (mon.isCanceled()) {
                return "Cancelled";
            }
            connection.setName(name);
            connection.setBaseURI(new URI(url));
            connection.setUsername(user);
            mon.setFraction(0.5);
            AuthToken token = AuthUtil.genAuthToken(user, form.getPassword(), connection.getConid(), mon);
            if (mon.isCanceled()) {
                return "Cancelled";
            }
            connection.setAuthToken(token);
            if (mon.isCanceled()) {
                return "Cancelled";
            }
            connection.connect();
            mon.setFraction(1.0);
        } catch (Exception e) {
            Throwable unwrap = Logger.unwrap(e);
            StackTraceElement[] stackTrace = unwrap.getStackTrace();
            String s = Arrays.toString(stackTrace);
            String msg = message("CodewindConnectionUpdateError", name);
            CoreUtil.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Messages.showErrorDialog( msg + "\n\n" + e.getLocalizedMessage() + "\n\n" + s, "Update Connection Error");
                }
            });
            return msg;
        } finally {
//            ViewHelper.openCodewindExplorerView();
//            CodewindUIPlugin.getUpdateHandler().updateConnection(connection);
            CoreUtil.updateAll();
        }







//        String name = form.getConnectionName();
//        String errorMessage = null;
//        connection = null;
//        String user = form.getUsername();
//        System.out.println("***** Editing connection for " + name + ", " + user);
//        try {
//            String url = form.getUrl();
//            URI uri = new URI(url);
//            String pass = form.getPassword();
//            System.out.println("***** PASSWORD TO USE IS " + pass);
//            ProgressIndicator indicator = ProgressManager.getInstance().getProgressIndicator();
//            String conid = ConnectionUtil.addConnection(name, uri.toString(), user, indicator);
//            if (indicator.isCanceled()) {
//                removeConnection(conid, indicator);
//                return "Cancelled";
//            }
//            // if conid is still null return??
//            connection = CodewindObjectFactory.createRemoteConnection(name, uri, conid, user, null);
//            AuthToken token = AuthUtil.genAuthToken(user, pass, conid, indicator);
//            if (indicator.isCanceled()) {
//                removeConnection(indicator);
//                return "Cancelled";
//            }
//            connection.setAuthToken(token);
//            connection.connect();
//            if (indicator.isCanceled()) {
//                removeConnection(indicator);
//                return "Cancelled";
//            }
//        } catch (Exception e) {
//            String msg;
//            if (connection == null) {
//                msg = message("CodewindConnectionCreateError", name);
//            } else {
//                msg = message("CodewindConnectionConnectError", name);
//            }
//            Throwable unwrap = Logger.unwrap(e);
//            StackTraceElement[] stackTrace = unwrap.getStackTrace();
//            String s = Arrays.toString(stackTrace);
//            CoreUtil.invokeLater(new Runnable() {
//                @Override
//                public void run() {
//                    Messages.showErrorDialog( msg + "\n\n" + e.getLocalizedMessage() + "\n\n" + s, "Connection Error");
//                }
//            });
//            return msg;
//        } finally {
//            if (connection != null) {
//                ConnectionManager.add(connection);
//                CoreUtil.updateAll();
////                ViewHelper.openCodewindExplorerView();
////                CodewindUIPlugin.getUpdateHandler().updateAll();
//            }
//        }
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

