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

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.ui.tasks.OpenIdeaProjectTask;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;

import java.util.Optional;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.*;

public class OpenIdeaProjectAction extends TreeAction {
    public OpenIdeaProjectAction() {
        super("Open Project");
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        Optional<CodewindApplication> application = selectionAsType(e, CodewindApplication.class);
        application.ifPresent(a -> ProgressManager.getInstance().run(new OpenIdeaProjectTask(a)));
    }
}
