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

import javax.swing.event.ChangeListener;

/**
 * The step is a change listener to any form or widget changes so that steps can change their next page target,
 * or other things.
 */
public abstract class AbstractCodewindWizardStep extends AbstractWizardStepEx implements ChangeListener {

    public AbstractCodewindWizardStep(String title) {
        super(title);
    }
    // These can change
    protected Object nextStepId;
    protected Object previousStepId;

    /**
     * This is intended to be called only by the wizard model
     */
    public void setNextStepId(Object id) {
        this.nextStepId = id;
    }

    /**
     * This is intended to be called only by the wizard model
     */
    public void setPreviousStepId(Object id) {
        this.previousStepId = id;
    }

    /**
     * Listeners can be added to be informed of any form or widget changes
     * @param listener
     */
    public abstract void addListener(ChangeListener listener);
}
