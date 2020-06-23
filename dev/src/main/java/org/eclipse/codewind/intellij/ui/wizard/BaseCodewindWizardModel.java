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

import java.util.ArrayList;
import java.util.List;

/**
 * This wizard model will handle the logic to determine the page ordering.  The subclass can define
 * the behavior based on listening to the change events from each step
 *
 * Each step should know which page is next based on the context they know about.  eg. a checkbox has
 * been selected or not.  The model will just facilitate updating all the steps' next and previous step IDs.
 */
public class BaseCodewindWizardModel {

    private List<? extends AbstractCodewindWizardStep> steps = new ArrayList<>();

    // List of all steps.  Each step must have a valid initial prev and next step ID.
    // Not all steps have to be on the wizard path sequence but can be added to the path depending
    // on each step's logic.
    public void addSteps(List<? extends AbstractCodewindWizardStep> steps) {
        this.steps = steps;
    }

    /**
     * There should be no invalid paths initially when the wizard is initialized.
     * For example
     * Initial main path: A - C, where A is always the first page*
     *     Anext = C, Aprev = null (The first page is where Xprev = null)
     *     Cnext = null, Cprev = A (The last page is where Xnext = null)
     * Other steps that are not on the initial wizard path
     *     Bnext = C, Bprev = A - Valid new path: A - B - C, essentially inserting step B between A - C.
     *     Bnext = C, Bprev = C - is invalid (If the page must be re-used, then a new step ID should be used)
     * When a new path is inserted, only one Xnext and one Yprev need to be changed in order to have a valid path.
     * The reason is that the context/widget/value changes on the current page determines what the next page will be.
     * It will not affect the previous page. So if Xnext changes, then only one Yprev value changes in order to complete
     * the wizard path back to the current page.
     *
     * For example, when B is inserted like so:  A - B - C
     * then the state changes are:
     *   Anext = B (NEW change, since original Anext = C)
     *   Bprev = A (originally defined in step B)
     *   Bnext = C (originally defined in step B)
     *   Cprev = B (NEW change, since original Cprev = A)
     *
     *
     * @param step
     * @param nextStepId
     */
    public void updateNextStep(AbstractCodewindWizardStep step, Object nextStepId) {
        Object currentStepId = step.getStepId();  // A, nextStepId = B
        Object currentNextStepId = step.getNextStepId(); // A's current next step is C
        // Because A's next step is going to change, the original next step's prev pointer must change
        // Find that step now.
        AbstractCodewindWizardStep oldStepPrevToUpdate = null;
        for (AbstractCodewindWizardStep aStep : steps) {
            if (aStep.getStepId() == currentNextStepId) {  // C
                oldStepPrevToUpdate = aStep; // C's prev pointer needs to be changed.  But to what?  We need to find the step that has C as the next step
            }
        }

        for (AbstractCodewindWizardStep aStep : steps) {
            if (aStep.getStepId() == nextStepId) {   // B
                aStep.setPreviousStepId(currentStepId);  // Bprev = A - Ensure Bprev is to A
            }
            if (oldStepPrevToUpdate != null && aStep.getNextStepId() == oldStepPrevToUpdate.getStepId()) { // C
                oldStepPrevToUpdate.setPreviousStepId(aStep.getStepId());
            }
        }
        step.setNextStepId(nextStepId);  // Anext = B
    }
}
