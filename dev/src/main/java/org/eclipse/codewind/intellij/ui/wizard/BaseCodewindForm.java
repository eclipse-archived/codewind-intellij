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

import com.intellij.util.EventDispatcher;

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BaseCodewindForm {

    protected final EventDispatcher<ChangeListener> eventDispatcher = EventDispatcher.create(ChangeListener.class);

    public void addListener(ChangeListener listener) {
        eventDispatcher.addListener(listener);
    }

    public void fireStateChanged(ChangeEvent event) {
        eventDispatcher.getMulticaster().stateChanged(event);
    }

}
