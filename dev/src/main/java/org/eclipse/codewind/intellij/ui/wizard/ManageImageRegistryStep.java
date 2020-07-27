package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.ImageRegistryForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class ManageImageRegistryStep extends AbstractWizardStepEx {
    public static String STEP_ID = "ManageImageRegistryStep";
    private CodewindConnection connection;
    private Project project;
    private ImageRegistryForm form;

    public ManageImageRegistryStep(@Nullable String title, Project project, CodewindConnection connection) {
        super(title);
        this.project = project;
        this.connection = connection;
    }

    @NotNull
    @Override
    public Object getStepId() {
        return STEP_ID;
    }

    @Nullable
    @Override
    public Object getNextStepId() {
        return null;
    }

    @Nullable
    @Override
    public Object getPreviousStepId() {
        return null;
    }

    @Override
    public boolean isComplete() {
        return form.isComplete();
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        String message = form.validatePage();
        if (message != null) {
            throw new CommitStepException(message);
        }
    }

    // Same as commit since it is only one page
    public void doOKAction() {
        form.doFinish();
    }

    @Override
    public JComponent getComponent() {
        if (form != null) {
            return form.getContentPane();
        }
        form = new ImageRegistryForm(this.project, this.connection);
        form.setDescriptionText(message("RegMgmtDescription"));

        ProgressIndicator progressIndicator = ProgressManager.getInstance().getProgressIndicator();
        try {
            form.update(progressIndicator);
        } catch (Exception e) {

        }

        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }
}
