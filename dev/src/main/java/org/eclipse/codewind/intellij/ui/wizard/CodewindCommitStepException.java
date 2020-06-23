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
package org.eclipse.codewind.intellij.ui.wizard;

import com.intellij.ide.wizard.CommitStepException;

import javax.swing.JComponent;

public class CodewindCommitStepException extends CommitStepException {

    private String title;
    private JComponent component;

    public CodewindCommitStepException(final String title, final String message, final JComponent component) {
        super(message);
        this.title = title;
        this.component = component;
    }

    public String getTitle() {
        return title;
    }

    public JComponent getComponent() {
        return component;
    }
}
