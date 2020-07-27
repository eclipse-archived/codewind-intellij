package org.eclipse.codewind.intellij.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.RemoteConnection;
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

public class EditConnectionAction extends AnAction {

    public EditConnectionAction() {
        super(message("EditConnectionActionLabel"));
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

        Object node = treePath.getLastPathComponent();
        CodewindTreeModel model = (CodewindTreeModel) tree.getModel();
        if (node instanceof RemoteConnection) {
            RemoteConnection remoteConnection = (RemoteConnection)node;
            List<AbstractConnectionWizardStep> steps = new ArrayList<>(); // message("EditConnectionDialogTitle")
            EditCodewindConnectionStep step = new EditCodewindConnectionStep(message("ConnectionPage_RemoteStep"), e.getProject(), remoteConnection);
            steps.add(step);
            EditConnectionWizard wizard = new EditConnectionWizard(message("EditConnectionDialogShell"), e.getProject(), steps, remoteConnection);
            boolean rc = wizard.showAndGet();
            if (rc) {
                ProgressManager.getInstance().run(new RefreshTask(node, tree));
            }
        }
    }
}
