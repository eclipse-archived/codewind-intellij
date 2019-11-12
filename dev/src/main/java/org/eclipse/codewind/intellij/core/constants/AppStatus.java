/*******************************************************************************
 * Copyright (c) 2018 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.constants;

import static org.eclipse.codewind.intellij.core.messages.CodewindCoreBundle.message;

import org.eclipse.codewind.intellij.core.Logger;

public enum AppStatus {

    STARTED("started", message("AppStatusStarted")),
    STARTING("starting", message("AppStatusStarting")),
    STOPPING("stopping", message("AppStatusStopping")),
    STOPPED("stopped", message("AppStatusStopped")),
    UNKNOWN("unknown", message("AppStatusUnknown"));

    public final String appStatus;
    public final String displayString;

    /**
     * @param appStatus - App state used by Codewind
     */
    private AppStatus(String appStatus, String displayString) {
        this.appStatus = appStatus;
        this.displayString = displayString;
    }

    public static AppStatus get(String appStatus) {
        for (AppStatus state : AppStatus.values()) {
            if (state.appStatus.equals(appStatus)) {
                return state;
            }
        }
        Logger.logWarning("Unrecognized application status: " + appStatus);
        return AppStatus.UNKNOWN;
    }

    public String getDisplayString(StartMode mode) {
        if (this == AppStatus.STARTED && StartMode.DEBUG_MODES.contains(mode)) {
            return message("AppStatusDebugging");
        }
        return displayString;
    }

}
