/*******************************************************************************
 * Copyright (c) 2020 IBM Corporation and others.
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
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.cli.InstallUtil;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class UninstallCodewindTask extends CodewindProcessTask {

    public UninstallCodewindTask(Runnable onSuccess) {
        super(null, message("RemovingCodewindJobLabel"), onSuccess);
    }

    @Override
    protected ProcessHelper.ProcessResult runProcess(@NotNull ProgressIndicator indicator) throws Exception {
        ProcessHelper.ProcessResult result = null;
        result = InstallUtil.stopCodewind(indicator);
        if (result.getExitValue()!= 0) {
            Logger.log("Error occurred stopping Codewind: " + result.getErrorMsg());
            System.out.println("*** Error occurred stopping Codewind: " + result.getErrorMsg());
        } else {
            result = InstallUtil.removeCodewind(null, indicator);
            if (result.getExitValue()!= 0) {
                Logger.log("Error occurred removing Codewind after install: " + result.getErrorMsg());
                System.out.println("*** Error occurred removing Codewind after install: " + result.getErrorMsg());
            }
        }
        return result;
    }

    @Override
    protected String getExceptionMessageKey() {
        String key = "InstallCodewindFailNoMessage";  // This message is generic to the installer, so we use it for install and uninstall
        message(key, "dummy");  // Tell IntelliJ this key is used.
        return key;
    }
}
