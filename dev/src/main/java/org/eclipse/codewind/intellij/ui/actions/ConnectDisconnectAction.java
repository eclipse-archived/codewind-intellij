package org.eclipse.codewind.intellij.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.ui.tasks.ConnectDisconnectTask;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.eclipse.codewind.intellij.ui.wizard.AbstractConnectionWizardStep;
import org.eclipse.codewind.intellij.ui.wizard.EditCodewindConnectionStep;
import org.eclipse.codewind.intellij.ui.wizard.EditConnectionWizard;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ConnectDisconnectAction extends AnAction {

    public ConnectDisconnectAction(boolean isConnectAction) {
        super( isConnectAction ? message("ConnectActionLabel") : message("DisconnectActionLabel"));
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.log("unrecognized component for : " + data);
            return;
        }

        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.log("no selection for: " + data);
            return;
        }
        Presentation presentation = getTemplatePresentation();
        String text = presentation.getText();
        Object node = treePath.getLastPathComponent();
        if (node instanceof RemoteConnection) {
            RemoteConnection remoteConnection = (RemoteConnection)node;
            ProgressManager.getInstance().run(new ConnectDisconnectTask(remoteConnection, e.getProject(), text));
        }
    }
}
