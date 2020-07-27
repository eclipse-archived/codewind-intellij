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
package org.eclipse.codewind.intellij.core.connection;

import com.intellij.openapi.progress.EmptyProgressIndicator;
import com.intellij.openapi.progress.ProgressIndicator;
import org.eclipse.codewind.filewatchers.core.FWAuthToken;
import org.eclipse.codewind.filewatchers.core.IAuthTokenProvider;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.cli.AuthToken;
import org.eclipse.codewind.intellij.core.cli.AuthUtil;
import org.json.JSONException;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class AuthManager implements IAuthTokenProvider {
    private final CodewindConnection connection;
    private AuthToken token;

    AuthManager(CodewindConnection connection, AuthToken token) {
        this.connection = connection;
        this.token = token;
    }

    AuthToken getToken(boolean update, ProgressIndicator monitor) throws IOException, JSONException {
        boolean needsUpdate = token == null || token.aboutToExpire() || update;
        if (needsUpdate) {
            updateToken(monitor);
        }
        return token;
    }

    synchronized void setToken(AuthToken token) {
        this.token = token;
    }

    synchronized void updateToken(ProgressIndicator monitor) throws IOException, JSONException {
        if (token != null && token.recentlyCreated()) {
            return;
        }
        try {
            token = AuthUtil.getAuthToken(connection.getUsername(), connection.getConid(), monitor);
        } catch (TimeoutException e) {
            throw new IOException("Timed out trying to update the token for connection: " + connection.getName());
        }
    }

    AuthToken getTokenNonBlocking() {
        return token;
    }

    void updateTokenNonBlocking() {
        Runnable runnable = () -> {
            try {
                updateToken(new EmptyProgressIndicator());
            } catch (Exception e) {
                Logger.logWarning("An error occurred trying to update the token for connection: " + connection.getName(), e);
            }
        };
        Thread thread = new Thread(runnable);
        thread.setDaemon(true);
        thread.run();
    }

    @Override
    public FWAuthToken getLatestAuthToken() {
        AuthToken token = getTokenNonBlocking();
        if (token != null && token.getToken() != null && token.getTokenType() != null) {
            return new FWAuthToken(token.getToken(), token.getTokenType());
        }
        return null;
    }

    @Override
    public void informReceivedInvalidAuthToken(FWAuthToken badToken) {
        updateTokenNonBlocking();
    }
}
