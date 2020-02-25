/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.ui.tasks;

import com.intellij.openapi.progress.PerformInBackgroundOption;
import com.intellij.openapi.progress.ProgressIndicator;
import org.eclipse.codewind.intellij.core.CodewindManager;
import org.eclipse.codewind.intellij.core.cli.InstallStatus;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class StartCodewindTask extends CodewindProcessTask {

    public StartCodewindTask(Runnable onSuccess) {
        super(null, message("StartingCodewindJobLabel"), false, PerformInBackgroundOption.DEAF, onSuccess);
    }

    @Override
    protected ProcessHelper.ProcessResult runProcess(@NotNull ProgressIndicator indicator) throws Exception {
        InstallStatus status = CodewindManager.getManager().getInstallStatus();
        return InstallUtil.startCodewind(status.getVersion(), indicator);
    }

    @Override
    protected String getExceptionMessageKey() {
        String key = "StartCodewindErrorWithMsg";
        message(key, "dummy");  // Tell IntelliJ this key is used.
        return key;
    }
}
