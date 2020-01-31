/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.codewind.intellij.core.filewatcher;

import java.io.File;
import java.util.UUID;

import org.eclipse.codewind.filewatchers.JavaNioWatchService;
import org.eclipse.codewind.filewatchers.core.Filewatcher;
import org.eclipse.codewind.filewatchers.core.IAuthTokenProvider;

/**
 * This class is responsible for kicking off the Filewatcher core code (via the
 * Filewatcher constructor), adding the resource change listener to workspace,
 * and receiving events from that Eclipse resource change listener then passing
 * those events to the core Filewatcher logic.
 *
 * Only a single instance of this class should exist per Codewind server. This
 * class is threadsafe.
 */
public class CodewindIntelliJFilewatcherdConnection {
    @SuppressWarnings("unused")
    private final String baseHttpUrl;

    private final Filewatcher fileWatcher;

    private final String clientUuid;

    public CodewindIntelliJFilewatcherdConnection(String baseHttpUrl, File pathToCwctl,
                                                  IAuthTokenProvider authTokenProvider /* nullable */) {

        if (pathToCwctl == null) {
            throw new RuntimeException("A valid path to the Codewind CLI is required: " + pathToCwctl);
        }

        if (!pathToCwctl.exists() || !pathToCwctl.canExecute()) {
            throw new RuntimeException(this.getClass().getSimpleName() + " was passed an invalid installer path: "
                    + pathToCwctl.getPath());
        }

        this.clientUuid = UUID.randomUUID().toString();

        String url = baseHttpUrl;
        if (!url.startsWith("http://") && !url.startsWith("https://")) {
            throw new IllegalArgumentException("Argument should begin with http:// or https://.");
        }

        this.fileWatcher = new Filewatcher(url, clientUuid, new JavaNioWatchService(), null, pathToCwctl.getPath(),
                authTokenProvider);

        this.baseHttpUrl = url;

    }

    public String getClientUuid() {
        return clientUuid;
    }

    public void dispose() {
        try {
            fileWatcher.dispose();
        } catch (Exception e) {
            /* ignore */
        }
    }
}


