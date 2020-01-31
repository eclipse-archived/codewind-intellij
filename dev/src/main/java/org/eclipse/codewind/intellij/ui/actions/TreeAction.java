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

package org.eclipse.codewind.intellij.ui.actions;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.ui.treeStructure.Tree;
import org.eclipse.codewind.intellij.core.Logger;
import org.jetbrains.annotations.NotNull;

import javax.swing.tree.TreePath;
import java.util.Optional;
import java.util.function.Function;

import static com.intellij.openapi.actionSystem.PlatformDataKeys.CONTEXT_COMPONENT;

public abstract class TreeAction<T> extends AnAction {
    private final String text;
    private final Class<T> type;
    private final Function<T, Task.Backgroundable> taskFactory;

    public TreeAction(String text, Class<T> type, Function<T, Task.Backgroundable> taskFactory) {
        super(text);
        this.text = text;
        this.type = type;
        this.taskFactory = taskFactory;
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent e) {
        getSelection(e).ifPresent(value -> ProgressManager.getInstance().run(taskFactory.apply(value)));
    }

    protected Optional<T> getSelection(@NotNull AnActionEvent e) {
        Object data = e.getData(CONTEXT_COMPONENT);
        if (!(data instanceof Tree)) {
            Logger.logWarning("unrecognized component for action " + text + ": " + data);
            System.out.println("unrecognized component for action " + text + ": " + data);
            return Optional.empty();
        }
        Tree tree = (Tree) data;
        TreePath treePath = tree.getSelectionPath();
        Object node = treePath.getLastPathComponent();
        if (!(type.isInstance(node))) {
            Logger.logWarning("unrecognized node for action " + text + ": " + node);
            System.out.println("unrecognized node for action " + text + ": " + node);
            return Optional.empty();
        }
        return Optional.of(type.cast(node));
    }
}
