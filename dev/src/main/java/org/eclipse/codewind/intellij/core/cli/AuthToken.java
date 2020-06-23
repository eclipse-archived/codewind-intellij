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

import org.eclipse.codewind.intellij.core.IAuthInfo;
import org.eclipse.codewind.intellij.core.connection.JSONObjectResult;
import org.json.JSONObject;

public class AuthToken extends JSONObjectResult implements IAuthInfo {
	
	private static final String ACCESS_TOKEN_KEY = "access_token";
	private static final String TOKEN_TYPE_KEY = "token_type";
	
	public AuthToken(JSONObject authToken) {
		super(authToken, "authorization token");
	}
	
	public String getToken() {
		return getString(ACCESS_TOKEN_KEY);
	}
	
	public String getTokenType() {
		return getString(TOKEN_TYPE_KEY);
	}

	@Override
	public boolean isValid() {
		return getToken() != null && getTokenType() != null;
	}

	@Override
	public String getHttpAuthorization() {
		return getTokenType() + " " + getToken();
	}


}
