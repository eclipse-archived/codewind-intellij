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
import org.eclipse.codewind.intellij.core.InstallUtil;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.jetbrains.annotations.NotNull;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class StopCodewindTask extends CodewindProcessTask {

    public StopCodewindTask(Runnable onSuccess) {
        super(null, message("StoppingCodewindJobLabel"), onSuccess);
    }

    @Override
    protected ProcessHelper.ProcessResult runProcess(@NotNull ProgressIndicator indicator) throws Exception {
        return InstallUtil.stopCodewind(indicator);
    }

    @Override
    protected String getExceptionMessageKey() {
        String key = "CodewindStopFail";
        message(key, "dummy");  // Tell IntelliJ this key is used.
        return key;
    }
}
