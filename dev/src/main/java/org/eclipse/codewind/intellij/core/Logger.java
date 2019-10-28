/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

public class Logger {

    private static com.intellij.openapi.diagnostic.Logger getLogger() {
        return com.intellij.openapi.diagnostic.Logger.getInstance("Codewind");
    }

    public static void logError(String msg) {
        getLogger().error(msg);
    }

    public static void logError(String msg, Throwable t) { getLogger().error(msg, t); }

    public static void logError(Throwable t) {
        getLogger().error(t);
    }

    public static void log(String msg) {
        getLogger().info(msg);
    }
}
