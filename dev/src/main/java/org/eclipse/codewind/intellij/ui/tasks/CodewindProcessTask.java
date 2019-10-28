/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.Messages;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public abstract class CodewindProcessTask extends Task.Backgroundable {

    protected final Runnable onSuccess;
    protected ProcessHelper.ProcessResult result;

    public CodewindProcessTask(@Nullable Project project,
                               @Nls(capitalization = Nls.Capitalization.Title) @NotNull String title,
                               Runnable onSuccess) {
        super(project, title);
        this.onSuccess = onSuccess;
    }

    @Override
    public final void run(@NotNull ProgressIndicator indicator) {
        try {
            result = runProcess(indicator);
        } catch (Exception e) {
            // This will be handled by #onThrowable
            throw new RuntimeException(e);
        }
    }

    // Any thrown Exception will be handled in the #onThrowable method
    protected abstract ProcessHelper.ProcessResult runProcess(@NotNull ProgressIndicator indicator) throws Exception;

    // Return the message key for the message used to report any exception
    protected abstract String getExceptionMessageKey();

    @Override
    public void onFinished() {
        if (result == null) {
            // This is due to an exception being thrown from #run().
            // It is handled by #onThrowable, so we just return.
            return;
        }

        if (result.getExitValue() == 0) {
            // Codewind was successfully started
            onSuccess.run();
            return;
        }

        // An error occurred.
        Logger.logError("Installer failed with return code: " + result.getExitValue() + ", output: " + result.getOutput() + ", error: " + result.getError()); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
        Messages.showErrorDialog(message(getExceptionMessageKey(), result.getError()), message("CodewindLabel"));
        super.onFinished();
    }

    @Override
    public void onThrowable(@NotNull Throwable error) {
        Throwable t = error;
        while (t.getCause() != null)
            t = t.getCause();
        Logger.logError("An error occurred starting Codewind: ", t); //$NON-NLS-1$
        Messages.showErrorDialog(message(getExceptionMessageKey(), t), message("CodewindLabel"));
    }
}
