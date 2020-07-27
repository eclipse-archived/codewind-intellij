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

import org.eclipse.codewind.intellij.core.connection.JSONObjectResult;
import org.json.JSONObject;

public class AuthToken extends JSONObjectResult {
	
	private static final String ACCESS_TOKEN_KEY = "access_token";
	private static final String TOKEN_TYPE_KEY = "token_type";
	private static final String EXPIRES_IN_KEY = "expires_in";

	private static long EXPIRY_BUFFER = 300 * 1000;
	private static long CREATE_BUFFER = 300 * 1000;

	private final long createTimeMillis;

	public AuthToken(JSONObject authToken) {
		super(authToken, "authorization token");
		createTimeMillis = System.currentTimeMillis();
	}
	
	public String getToken() {
		return getString(ACCESS_TOKEN_KEY);
	}
	
	public String getTokenType() {
		return getString(TOKEN_TYPE_KEY);
	}

	public Integer getExpiresInSeconds() {
		return getInt(EXPIRES_IN_KEY);
	}

	public long getCreateTimeMillis() {
		return createTimeMillis;
	}

	public boolean aboutToExpire() {
		Integer expiresInSeconds = getExpiresInSeconds();
		if (expiresInSeconds == null) {
			return false;
		}
		long expiryTime = createTimeMillis + (expiresInSeconds * 1000);
		return System.currentTimeMillis() > (expiryTime - EXPIRY_BUFFER);
	}

	public boolean recentlyCreated() {
		return (createTimeMillis + CREATE_BUFFER) > System.currentTimeMillis();
	}

}
