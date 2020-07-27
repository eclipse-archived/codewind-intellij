package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.form.AddImageRegistryForm;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public class AddImageRegistryStep extends AbstractWizardStepEx {

    public static String STEP_ID = "AddImageRegistryStep";
    private CodewindConnection connection;
    private Project project;
    private AddImageRegistryForm form;

    public AddImageRegistryStep(@Nullable String title, Project project, CodewindConnection connection) {
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
        // return form.isComplete()
        return true;
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
//        String message = form.validatePage();
//        if (message != null) {
//            throw new CommitStepException(message);
//        }
    }

    @Override
    public JComponent getComponent() {
        if (form != null) {
            return form.getContentPane();
        }
        form = new AddImageRegistryForm();
        return form.getContentPane();
    }

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }
}
