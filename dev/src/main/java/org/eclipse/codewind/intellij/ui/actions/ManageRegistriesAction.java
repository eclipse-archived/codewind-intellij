package org.eclipse.codewind.intellij.ui.actions;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
import org.eclipse.codewind.intellij.ui.tasks.RefreshTask;
import org.eclipse.codewind.intellij.ui.wizard.ManageImageRegistryStep;
import org.eclipse.codewind.intellij.ui.wizard.ManageImageRegistryWizard;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.ArrayList;
import java.util.List;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;
import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ManageRegistriesAction extends AnAction {

    public ManageRegistriesAction() {
        super(message("RegMgmtActionLabel"));
    }

    @Override
    public void update(@NotNull AnActionEvent e) {
        super.update(e);
        // TODO enable these two lines
//        CodewindConnection connection = getSelection(e);
//        e.getPresentation().setEnabled(connection != null && connection.isConnected());
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        CodewindConnection connection = getSelection(e);

        if (connection instanceof RemoteConnection) {
            RemoteConnection remoteConnection = (RemoteConnection)connection;
            List<AbstractWizardStepEx> steps = new ArrayList<>();
            ManageImageRegistryStep step = new ManageImageRegistryStep(message("RegMgmtManageStep"), e.getProject(), remoteConnection);
            steps.add(step);
            ManageImageRegistryWizard wizard = new ManageImageRegistryWizard(message("RegMgmtDialogTitle"), e.getProject(), steps);
            boolean rc = wizard.showAndGet();
            if (rc) {
                Object data = e.getData(CONTEXT_COMPONENT);
                if (data instanceof Tree) {
                    Tree tree = (Tree) data;
                    ProgressManager.getInstance().run(new RefreshTask(connection, tree));
                }
            }
        }
    }

    private CodewindConnection getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.logDebug("Unrecognized component for : " + data);
            return null;
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        if (treePath == null) {
            Logger.logDebug("No selection path for ManageRegistriesAction: " + tree);
            return null;
        }
        Object node = treePath.getLastPathComponent();
        if (!(node instanceof CodewindConnection)) {
            return null;
        }
        return (CodewindConnection) node;
    }
}
