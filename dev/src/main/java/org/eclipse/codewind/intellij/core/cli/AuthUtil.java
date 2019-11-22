/*******************************************************************************
 * Copyright (c) 2019 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *	 IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.cli;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import com.intellij.openapi.progress.ProgressIndicator;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.ProcessHelper.ProcessResult;
import org.json.JSONException;
import org.json.JSONObject;

public class AuthUtil {
	

	private static final String SECKEYRING_CMD = "seckeyring";
	private static final String UPDATE_OPTION = "update";
	
	private static final String SECTOKEN_CMD = "sectoken";
	private static final String GET_OPTION = "get";
	
	private static final String USERNAME_OPTION = "--username";
	private static final String PASSWORD_OPTION = "--password";
	
	private static final String STATUS_KEY = "status";
	private static final String STATUS_MSG_KEY = "status_message";
	
	private static final String STATUS_OK_VALUE = "OK";
	
	public static AuthToken getAuthToken(String username, String password, String conid, ProgressIndicator indicator) throws IOException, JSONException, TimeoutException {
		indicator.setIndeterminate(true);
		Process process = null;
		try {
			process = CLIUtil.runCWCTL(null, new String[] {SECKEYRING_CMD, UPDATE_OPTION}, new String[] {USERNAME_OPTION, username, PASSWORD_OPTION, password, CLIUtil.CON_ID_OPTION, conid});
			ProcessResult result = ProcessHelper.waitForProcess(process, 500, 60);
			if (result.getExitValue() != 0) {
				Logger.logWarning("Seckeyring update failed with rc: " + result.getExitValue() + " and error: " + result.getErrorMsg()); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IOException(result.getErrorMsg());
			}
			if (result.getOutput() == null || result.getOutput().trim().isEmpty()) {
				// This should not happen
				Logger.logWarning("Seckeyring update had 0 return code but the output is empty"); //$NON-NLS-1$
				throw new IOException("The output from seckeyring update is empty."); //$NON-NLS-1$
			}
			JSONObject resultJson = new JSONObject(result.getOutput());
			if (!STATUS_OK_VALUE.equals(resultJson.getString(STATUS_KEY))) {
				String msg = "Seckeyring update failed for: " + conid + " with output: " + resultJson.getString(STATUS_MSG_KEY); //$NON-NLS-1$ //$NON-NLS-2$
				Logger.logWarning(msg);
				throw new IOException(msg);
			}
			
			return getAuthToken(username, conid, null);
		} finally {
			if (process != null && process.isAlive()) {
				process.destroy();
			}
		}
	}
	
	public static AuthToken getAuthToken(String username, String conid, ProgressIndicator monitor) throws IOException, JSONException, TimeoutException {
		if (monitor != null)
			monitor.setIndeterminate(true);
		Process process = null;
		try {
			process = CLIUtil.runCWCTL(new String[] {CLIUtil.INSECURE_OPTION}, new String[] {SECTOKEN_CMD, GET_OPTION}, new String[] {USERNAME_OPTION, username, CLIUtil.CON_ID_OPTION, conid});
			ProcessResult result = ProcessHelper.waitForProcess(process, 500, 60);
			if (result.getExitValue() != 0) {
				Logger.logWarning("Sectoken get failed with rc: " + result.getExitValue() + " and error: " + result.getErrorMsg()); //$NON-NLS-1$ //$NON-NLS-2$
				throw new IOException(result.getErrorMsg());
			}
			if (result.getOutput() == null || result.getOutput().trim().isEmpty()) {
				// This should not happen
				Logger.logWarning("Sectoken get had 0 return code but the output is empty"); //$NON-NLS-1$
				throw new IOException("The output from sectoken get is empty."); //$NON-NLS-1$
			}
			return new AuthToken(new JSONObject(result.getOutput()));
		} finally {
			if (process != null && process.isAlive()) {
				process.destroy();
			}
		}
	}
}
