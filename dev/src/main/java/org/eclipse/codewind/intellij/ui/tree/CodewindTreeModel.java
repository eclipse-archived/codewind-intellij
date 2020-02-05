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

package org.eclipse.codewind.intellij.ui.tree;

import com.intellij.ui.tree.BaseTreeModel;
import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.core.IUpdateHandler;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.eclipse.codewind.intellij.core.connection.ConnectionManager;

import javax.swing.tree.TreePath;
import java.util.Collections;
import java.util.List;

public class CodewindTreeModel extends BaseTreeModel<Object> implements IUpdateHandler {

    private static final CodewindTreeModel INSTANCE = new CodewindTreeModel();

    private Object root = ConnectionManager.getManager();

    @Override
    public List<? extends Object> getChildren(Object parent) {
        if (parent instanceof ConnectionManager) {
            return getChildren((ConnectionManager) parent);
        }
        if (parent instanceof CodewindConnection) {
            return getChildren((CodewindConnection) parent);
        }
        return Collections.emptyList();
    }

    private List<CodewindConnection> getChildren(ConnectionManager manager) {
        return manager.activeConnections();
    }

    private List<CodewindApplication> getChildren(CodewindConnection connection) {
        return connection.getSortedApps();
    }

    @Override
    public Object getRoot() {
        return root;
    }

    @Override
    public void updateAll() {
        this.treeStructureChanged(new TreePath(getRoot()), new int[0], new Object[0]);
    }

    @Override
    public void updateConnection(CodewindConnection connection) {
        int index = getChildren(getRoot()).indexOf(connection);
        if (index >= 0) {
            TreePath path = treePathFrom(getRoot(), connection);
            this.treeStructureChanged(path, new int[0], new Object[0]);
        } else {
            TreePath path = treePathFrom(getRoot());
            this.treeStructureChanged(path, new int[0], new Object[0]);
        }
    }

    @Override
    public void updateApplication(CodewindApplication application) {
        TreePath path = treePathFrom(getRoot(), application.getConnection());
        int index = getChildren(application.getConnection()).indexOf(application);
        if (index >= 0) {
            this.treeNodesChanged(path, new int[]{index}, new Object[]{application});
        } else {
            this.treeStructureChanged(path, new int[0], new Object[0]);
        }
    }

    @Override
    public void removeConnection(List<CodewindApplication> apps) {
        this.treeStructureChanged(new TreePath(getRoot()), new int[0], new Object[0]);
    }

    @Override
    public void removeApplication(CodewindApplication application) {
        updateConnection(application.getConnection());
    }

    private static TreePath treePathFrom(Object... objects) {
        return new TreePath(objects);
    }

    public static CodewindTreeModel getInstance() {
        return INSTANCE;
    }
}
