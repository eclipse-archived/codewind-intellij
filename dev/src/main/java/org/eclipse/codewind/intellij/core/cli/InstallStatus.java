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

package org.eclipse.codewind.intellij.core.cli;

import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.connection.CodewindConnection;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class InstallStatus {
	
	public static final String STATUS_KEY = "status";
	public static final String URL_KEY = "url";
	public static final String INSTALLED_VERSIONS_KEY = "installed-versions";
	public static final String STARTED_KEY = "started";

	public static final String WS_UPGRADE_VERSION = "0.6.0";

	public static final InstallStatus UNKNOWN = new InstallStatus(Status.UNKNOWN);

	private Status status;
	private String url;
	private JSONArray installedVersions;
	private JSONArray startedVersions;
	private String supportedVersion;
	
	private enum Status {
		UNINSTALLED("uninstalled"),
		STOPPED("stopped"),
		STARTED("started"),
		ERROR("error"),
		UNKNOWN("unknown");

		private String value;

		private Status(String value) {
			this.value = value;
		}

		public static Status getStatus(String statusStr) {
			for (Status status : Status.values()) {
				if (status.value.equals(statusStr)) {
					return status;
				}
			}
			// This should not happen
			Logger.logWarning("Unrecognized installer status: " + statusStr);
			return ERROR;
		}

		public boolean isInstalled() {
			return this == STARTED || this == STOPPED;
		}
	}
   
	public InstallStatus(JSONObject statusObj) {
		try {
			status = Status.getStatus(statusObj.getString(STATUS_KEY));
			if (status == Status.STARTED) {
				startedVersions = statusObj.getJSONArray(STARTED_KEY);
				supportedVersion = getSupportedVersion(startedVersions);
				url = statusObj.getString(URL_KEY);
				if (!url.endsWith("/")) {
					url = url + "/";
				}
			}
			if (status.isInstalled()) {
				installedVersions = statusObj.getJSONArray(INSTALLED_VERSIONS_KEY);
				if (supportedVersion == null) {
					supportedVersion = getSupportedVersion(installedVersions);
				}
				if (installedVersions.length() == 0 && startedVersions != null) {
					installedVersions = startedVersions;
				}
			}
			
		} catch (JSONException e) {
			Logger.logWarning("The Codewind installer status format is not recognized", e); //$NON-NLS-1$
			status = Status.ERROR;
			url = null;
		}
	}
	
	private String getSupportedVersion(JSONArray versions) throws JSONException {
		for (int i = 0; i < versions.length(); i++) {
			String version = versions.getString(i);
			if (CodewindConnection.isSupportedVersion(version) && InstallUtil.getVersion().equals(version)) {
				return version;
			}
		}
		return null;
	}
	
	private InstallStatus(Status status) {
		this.status = status;
	}
	
	public boolean isUnknown() {
		return status == Status.UNKNOWN;
	}

	public boolean isError() {
		return status == Status.ERROR;
	}

	public boolean isInstalled() {
		return supportedVersion != null;
	}
	
	public boolean isStarted() {
		try {
			if (supportedVersion != null && status == Status.STARTED) {
				for (int i = 0; i < startedVersions.length(); i++) {
					if (supportedVersion.equals(startedVersions.getString(i))) {
						return true;
					}
				}
			}
		} catch (JSONException e) {
			Logger.logWarning("The Codewind installer status format is not recognized", e); //$NON-NLS-1$
		}
		return false;
	}
	
	public String getVersion() {
		return supportedVersion;
	}
	
	public String getURL() {
		return url;
	}
	
	public boolean hasInstalledVersions() {
		return (installedVersions != null && installedVersions.length() > 0);
	}
	
	public boolean hasStartedVersions() {
		return (startedVersions != null && startedVersions.length() > 0);
	}
	
	public String getInstalledVersions() {
		return getVersionList(installedVersions);
	}
	
	public String getStartedVersions() {
		return getVersionList(startedVersions);
	}
	
	public String getVersionList(JSONArray versions) {
		StringBuilder builder = new StringBuilder();
		boolean start = true;
		for (int i = 0; i < versions.length(); i++) {
			try {
				if (!start) {
					builder.append(", ");
				}
				builder.append(versions.getString(i));
				start = false;
			} catch (JSONException e) {
				Logger.logWarning("The Codewind installer status format is not recognized", e); //$NON-NLS-1$
			}
		}
		return builder.toString();
	}

	@Override
	public String toString() {
		return "InstallStatus: { Status: " + status + ", URL: " + url + " }";
	}
}
