/*******************************************************************************
 * Copyright (c) 2018, 2019 IBM Corporation and others.
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

public enum BuildStatus {

    IN_PROGRESS("inProgress", message("BuildStateInProgress")),
    SUCCESS("success", message("BuildStateSuccess")),
    FAILED("failed", message("BuildStateFailed")),
    QUEUED("queued", message("BuildStateQueued")),
    UNKOWN("unknown", message("BuildStateUnknown"));

    public static final String BUILD_REQUIRED = "buildRequired";

    public final String status;
    public final String displayString;

    /**
     * @param buildStatus - Internal build status used by Codewind
     */
    private BuildStatus(String buildStatus, String displayString) {
        this.status = buildStatus;
        this.displayString = displayString;
    }

    public static BuildStatus get(String buildStatus) {
        if (BUILD_REQUIRED.equals(buildStatus)) {
            return null;
        }
        for (BuildStatus status : BuildStatus.values()) {
            if (status.status.equals(buildStatus)) {
                return status;
            }
        }
        Logger.logWarning("Unrecognized application state: " + buildStatus);
        return BuildStatus.UNKOWN;
    }

    public boolean isComplete() {
        return this == SUCCESS || this == FAILED;
    }

    public String getDisplayString() {
        return displayString;
    }

}
