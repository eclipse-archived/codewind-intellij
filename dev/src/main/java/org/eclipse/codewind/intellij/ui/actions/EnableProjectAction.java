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

package org.eclipse.codewind.intellij.ui.actions;

import org.eclipse.codewind.intellij.core.CodewindApplication;
import org.eclipse.codewind.intellij.ui.tasks.ProjectEnablementTask;

import static org.eclipse.codewind.intellij.ui.messages.CodewindUIBundle.message;

public class EnableProjectAction extends TreeAction<CodewindApplication> {

    public EnableProjectAction() {
        super(message("EnableProjectLabel"), CodewindApplication.class, ProjectEnablementTask::createEnabler);
    }
}
