/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core;

import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    /*
     * Use this class-specific-category-based logger for info and warnings
     */
    @NotNull
    private static com.intellij.openapi.diagnostic.Logger getLogger() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 2)
            return com.intellij.openapi.diagnostic.Logger.getInstance(CoreConstants.LOGGER_CATEGORY);

        // First element is the call to Thread::getStackTrace()
        // Second element is the call to this method
        Stream<StackTraceElement> stream = Arrays.stream(stackTrace, 2, stackTrace.length);
        String category = stream
                .map(StackTraceElement::getClassName)
                .filter(className -> !className.equals(Logger.class.getName()))
                .findFirst()
                .map(name -> "#" + name)
                .orElse(CoreConstants.LOGGER_CATEGORY);
        return com.intellij.openapi.diagnostic.Logger.getInstance(category);
    }

    /*
     * Use this general logger for debug and trace.  Use this in conjunction with getCaller() to also show the calling
     * class with the message
     */
    @NotNull
    private static com.intellij.openapi.diagnostic.Logger getGeneralLogger() {
        return com.intellij.openapi.diagnostic.Logger.getInstance(CoreConstants.LOGGER_CATEGORY);  // Use general codewind category
    }

    /*
     * Used to calculate the class that called the logger. Using the %C conversion pattern in log4J is not advised and affects performance.
     * So prefixing each of our messages with our 'calculated' caller class will not be redundant.
     *
     * @return fully qualified class name
     */
    @Nullable
    private static String getCaller() {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        if (stackTrace.length < 2)
            return null;

        // First element is the call to Thread::getStackTrace()
        // Second element is the call to this method
        Stream<StackTraceElement> stream = Arrays.stream(stackTrace, 2, stackTrace.length);
        String caller = stream
                .map(StackTraceElement::getClassName)
                .filter(className -> !className.equals(Logger.class.getName()))
                .findFirst()
                .orElse(null);
        return caller;
    }

    private static String getPrefixedMessage(String caller, String msg) {
        return caller != null ? caller + " - " + msg : msg;
    }

    /**
     * Log message at debug level. Enable Codewind logger category with debug.
     *
     * @param msg
     */
    public static void logDebug(String msg) {
        com.intellij.openapi.diagnostic.Logger logger = getGeneralLogger();
        if (logger.isDebugEnabled()) {
            String caller = getCaller();
            logger.debug(getPrefixedMessage(caller, msg));
        }
    }

    /**
     *  Log message at debug level and Throwable. Enable Codewind logger category with debug.
     *
     * @param msg
     * @param t
     */
    public static void logDebug(String msg, Throwable t) {
        com.intellij.openapi.diagnostic.Logger logger = getGeneralLogger();
        if (logger.isDebugEnabled()) {
            String caller = getCaller();
            logger.debug(getPrefixedMessage(caller, msg), t);
        }
    }

    /**
     * Log message at trace level. Enable Codewind logger category with :trace.
     * See @link(CoreConstants.LOGGER_CATEGORY)
     *
     * @param msg
     */
    public static void logTrace(String msg) {
        com.intellij.openapi.diagnostic.Logger logger = getGeneralLogger();
        if (logger.isTraceEnabled()) {
            String caller = getCaller();
            logger.trace(getPrefixedMessage(caller, msg));
        }
    }

    /**
     * Log throwable at trace level. Enable Codewind logger category with :trace.
     *
     * @param t
     */
    public static void logTrace(Throwable t) {
        com.intellij.openapi.diagnostic.Logger logger = getGeneralLogger();
        if (logger.isTraceEnabled()) {
            logger.trace(t);
        }
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

    /**
     * Use INFO sparingly since it will inundate the IntelliJ logs, eg. idea.log, with our output. By default, INFO
     * messages are sent to the log.
     *
     * @param msg
     */
    public static void log(String msg) {
        getLogger().info(msg);
    }

    /**
     * Use INFO sparingly since it will inundate the IntelliJ logs, eg. idea.log, with our output. By default, INFO
     * messages are sent to the log.
     *
     * @param msg
     * @param t
     */
    public static void log(String msg, Throwable t) {
        getLogger().info(msg, t);
    }

    /**
     * Use INFO sparingly since it will inundate the IntelliJ logs, eg. idea.log, with our output.  By default, INFO
     * messages are sent to the log.
     *
     * @param t
     */
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
