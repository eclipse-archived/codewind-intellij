package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.ide.wizard.CommitStepException;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.ui.form.RemoteConnectionForm;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;

public abstract class AbstractConnectionWizardStep extends AbstractWizardStepEx {

    protected Project project;
    protected RemoteConnectionForm form;

    public AbstractConnectionWizardStep(@Nullable String title) {
        super(title);
    }

    public void fireStateChanging() {
        this.fireStateChanged();
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

    @Nullable
    @Override
    public JComponent getPreferredFocusedComponent() {
        return null;
    }

    @Override
    public void commit(CommitType commitType) throws CommitStepException {
        String message = form.validatePage();
        if (message != null) {
            throw new CommitStepException(message);
        }
    }

}
