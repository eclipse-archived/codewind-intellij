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

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.stream.Stream;

/**
 * Class to write logging information to the IntelliJ log
 *
 * Note: IntelliJ considers actual errors written to the log to be a "Fatal IDE Error" and will show an error
 * notification to the user, asking if they want to disable the offending plugin.  So we shouldn't use those methods
 * unless it really is a fatal error.
 */
public class Logger {

    @NotNull
    private static com.intellij.openapi.diagnostic.Logger getLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 2)
            return com.intellij.openapi.diagnostic.Logger.getInstance("Codewind");

        // First element is the call to Thread::getStackTrace()
        // Second element is the call to this method
        Stream<StackTraceElement> stream = Arrays.stream(stackTrace, 2, stackTrace.length);
        String category = stream
                .map(StackTraceElement::getClassName)
                .filter(className -> !className.equals(Logger.class.getName()))
                .findFirst()
                .map(name -> "#" + name)
                .orElse("Codewind");
        return com.intellij.openapi.diagnostic.Logger.getInstance(category);
    }

    public static void logWarning(String msg) {
        getLogger().warn(msg);
    }

    public static void logWarning(String msg, Throwable t) {
        getLogger().warn(msg, t);
    }

    public static void logWarning(Throwable t) {
        getLogger().warn(t);
    }

    public static void log(String msg) {
        getLogger().info(msg);
    }

    public static void log(String msg, Throwable t) {
        getLogger().info(msg, t);
    }

    public static void log(Throwable t) {
        getLogger().info(t);
    }

    public static Throwable unwrap(Throwable error) {
        Throwable t = error;
        while (t.getCause() != null)
            t = t.getCause();
        return t;
    }
}
