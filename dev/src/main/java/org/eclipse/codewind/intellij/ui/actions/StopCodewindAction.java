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

package org.eclipse.codewind.intellij.ui.actions;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.ui.tasks.StartCodewindTask;
import org.eclipse.codewind.intellij.ui.tasks.StopCodewindTask;
import org.eclipse.codewind.intellij.ui.tree.CodewindTreeModel;
import org.jetbrains.annotations.NotNull;

public class StopCodewindAction extends AnAction {

    private final Runnable onSuccess;

    public StopCodewindAction(Runnable onSuccess) {
        super(message("InstallerActionStopLabel"));
        this.onSuccess = onSuccess;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        ProgressManager.getInstance().run(new StopCodewindTask(onSuccess));
    }
}
