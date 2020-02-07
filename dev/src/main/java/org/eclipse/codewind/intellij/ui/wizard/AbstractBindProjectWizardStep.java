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

import com.intellij.ide.wizard.AbstractWizardStepEx;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBindProjectWizardStep extends AbstractWizardStepEx {

    public AbstractBindProjectWizardStep(@Nullable String title) {
        super(title);
    }

    protected abstract void onStepEntering(BindProjectModel model);

    protected abstract void onStepLeaving(BindProjectModel model);

    protected abstract void postDoNextStep(BindProjectModel model);

    public void fireStateChanging() {
        this.fireStateChanged();
    }
}
