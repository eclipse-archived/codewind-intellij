package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.browsers.BrowserLauncher;
import com.intellij.ide.wizard.AbstractWizardEx;
import com.intellij.ide.wizard.AbstractWizardStepEx;
import com.intellij.openapi.project.Project;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.ui.constants.UIConstants;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

public class EditConnectionWizard  extends AbstractWizardEx {

    public EditConnectionWizard(String title, @Nullable Project project, List<? extends AbstractConnectionWizardStep> steps, CodewindConnection connection) {
        super(title, project, steps);
    }

    @Nullable
    @Override
    protected String getHelpID() {
        return null;
    }

    @Override
    protected void doOKAction() {
        super.doOKAction();
        EditCodewindConnectionStep currentStepObject = (EditCodewindConnectionStep)getCurrentStepObject();
        currentStepObject.doOKAction();
    }

    @Override
    protected boolean canFinish() {
        boolean canFinish = super.canFinish();
        return canFinish;
    }

    protected void helpAction() {
        try {
            BrowserLauncher.getInstance().browse(new URI(UIConstants.REMOTE_DEPLOY_URL));
        } catch (URISyntaxException ex) {
            Logger.logWarning(ex);
        }
    }
}
