/*******************************************************************************
 * Copyright (c) 2019, 2020 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v2.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v20.html
 *
 * Contributors:
 *	 IBM Corporation - initial API and implementation
 *******************************************************************************/

package org.eclipse.codewind.intellij.core.cli;

import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.PlatformUtil;
import org.eclipse.codewind.intellij.core.PlatformUtil.OperatingSystem;
import org.eclipse.codewind.intellij.core.ProcessHelper.ProcessResult;
import org.eclipse.codewind.intellij.core.constants.CoreConstants;
import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.*;
import java.util.stream.Collectors;

public class CLIUtil {

    public static final Path CODEWIND_DIR = Paths.get(System.getProperty("user.home"), ".codewind");

	// Global options
	public static final String JSON_OPTION = "--json";
	public static final String INSECURE_OPTION = "--insecure";
	public static final String[] GLOBAL_JSON = new String[] {JSON_OPTION};
	public static final String[] GLOBAL_INSECURE = new String[] {INSECURE_OPTION};
	public static final String[] GLOBAL_JSON_INSECURE = new String[] {JSON_OPTION, INSECURE_OPTION};
	
	// Common options
	public static final String CON_ID_OPTION = "--conid";

	// Common keys
	public static final String ERROR_KEY = "error";
	public static final String ERROR_DESCRIPTION_KEY = "error_description";
	private static final String STATUS_KEY = "status";
	private static final String STATUS_MSG_KEY = "status_message";
	
	// Common values
	private static final String STATUS_OK_VALUE = "OK";

    private static final Map<OperatingSystem, String> cwctlMap = new HashMap<>();
    private static final Map<OperatingSystem, String> appsodyMap = new HashMap<>();

    static {
        cwctlMap.put(OperatingSystem.LINUX, "cwctl/linux/cwctl");
        cwctlMap.put(OperatingSystem.MAC, "cwctl/darwin/cwctl");
        cwctlMap.put(OperatingSystem.WINDOWS, "cwctl/windows/cwctl.exe");
    }

    static {
        appsodyMap.put(OperatingSystem.LINUX, "cwctl/linux/appsody");
        appsodyMap.put(OperatingSystem.MAC, "cwctl/darwin/appsody");
        appsodyMap.put(OperatingSystem.WINDOWS, "cwctl/windows/appsody.exe");
    }

    private static final CLIInfo codewindInfo = new CLIInfo("Codewind", cwctlMap);
    private static final CLIInfo appsodyInfo = new CLIInfo("Appsody", appsodyMap);
    private static final CLIInfo[] cliInfos = {codewindInfo, appsodyInfo};

	public static Process runCWCTL(String[] globalOptions, String[] cmd, String[] options) throws IOException {
		return runCWCTL(globalOptions, cmd, options, null);
    }

	public static Process runCWCTL(String[] globalOptions, String[] cmd, String[] options, String[] args) throws IOException {
        return createCWCTLProcess(globalOptions, cmd, options, args).start();
    }

    @NotNull
    public static ProcessBuilder createCWCTLProcess(String[] globalOptions, String[] cmd, String[] options, String[] args) throws IOException {
        // Make sure the executables are installed
        for (int i = 0; i < cliInfos.length; i++) {
            if (cliInfos[i] != null)
                cliInfos[i].setInstallPath(getCLIExecutable(cliInfos[i]));
        }

        List<String> cmdList = new ArrayList<String>();
        cmdList.add(codewindInfo.getInstallPath());
        addOptions(cmdList, globalOptions);
        addOptions(cmdList, cmd);
        addOptions(cmdList, options);
        addOptions(cmdList, args);
        Logger.log(cmdList.stream().collect(Collectors.joining(" ")));
        String[] command = cmdList.toArray(new String[cmdList.size()]);
        ProcessBuilder builder = new ProcessBuilder(command);
        if (PlatformUtil.getOS() == PlatformUtil.OperatingSystem.MAC) {
            String pathVar = System.getenv(CoreConstants.PATH);
            pathVar = CoreConstants.USR_LOCAL_BIN + File.pathSeparator + pathVar;
            Map<String, String> env = builder.environment();
            env.put(CoreConstants.PATH, pathVar);
        }
        return builder;
    }

	private static void addOptions(List<String> cmdList, String[] options) {
		if (options != null) {
			for (String opt : options) {
				cmdList.add(opt);
			}
		}
	}
	
    public static String getCWCTLExecutable() throws IOException {
        return getCLIExecutable(codewindInfo);
    }

    public static String getCLIExecutable(CLIInfo operation) throws IOException {
        String installPath = operation.getInstallPath();
        if (installPath != null && (new File(installPath).exists())) {
            return installPath;
        }

        // Get the current platform and choose the correct executable path
        OperatingSystem os = PlatformUtil.getOS(System.getProperty("os.name"));

        Map<OperatingSystem, String> osPathMap = operation.getOSPathMap();
        if (osPathMap == null) {
            String msg = "Failed to get the list of operating specific paths for installing the executable " + operation.getInstallName();
            Logger.logWarning(msg);
            throw new IOException(msg);
        }

        String relPath = osPathMap.get(os);
        if (relPath == null) {
            String msg = "Failed to get the relative path for the install executable " + operation.getInstallName();
            Logger.logWarning(msg);
            throw new IOException(msg);
        }

        // Get the executable path
        String installerDir = getCLIInstallDir();
        String execName = relPath.substring(relPath.lastIndexOf('/') + 1);
        String execPath = installerDir + File.separator + execName;

        // Make the installer directory
        if (!FileUtil.makeDir(installerDir)) {
            String msg = "Failed to make the directory for the installer utility: " + installerDir;
            Logger.logWarning(msg);
            throw new IOException(msg);
        }

        // Copy the executable over
        try (InputStream stream = InstallUtil.class.getClassLoader().getResourceAsStream(relPath)) {
            if (stream == null) {
                throw new FileNotFoundException(relPath);
            }
            FileUtil.copyFile(stream, execPath);
            if (PlatformUtil.getOS() != PlatformUtil.OperatingSystem.WINDOWS) {
                Set<PosixFilePermission> permissions = PosixFilePermissions.fromString("rwxr-xr-x");
                File file = new File(execPath);
                Files.setPosixFilePermissions(file.toPath(), permissions);
            }
            return execPath;
        }
    }

    private static String getCLIInstallDir() {
        return CODEWIND_DIR.resolve(InstallUtil.getVersion()).toString();
	}
	
	public static void checkResult(String[] command, ProcessResult result, boolean checkOutput) throws IOException {
		// Check for json error output (may still get a 0 return code in this case)
		// Throws an exception if there is an error
		checkErrorResult(command, result);

		if (result.getExitValue() != 0) {
			String msg;
			String error = result.getError() != null && !result.getError().isEmpty() ? result.getError() : result.getOutput();
			if (error == null || error.isEmpty()) {
				msg = String.format("The %s command exited with return code %d", Arrays.toString(command), result.getExitValue()); //$NON-NLS-1$
			} else {
				msg = String.format("The %s command exited with return code %d and error: %s", Arrays.toString(command), result.getExitValue(), error); //$NON-NLS-1$
			}
			Logger.logWarning(msg);
			throw new IOException(msg);
		} else if (checkOutput && (result.getOutput() == null || result.getOutput().isEmpty())) {
			String msg = String.format("The %s command exited with return code 0 but the output was empty", Arrays.toString(command));  //$NON-NLS-1$
			Logger.logWarning(msg);
			throw new IOException(msg);
		}
	}

	private static void checkErrorResult(String[] command, ProcessResult result) throws IOException {
		try {
			if (result.getOutput() != null && !result.getOutput().isEmpty()) {
				JSONObject obj = new JSONObject(result.getOutput());
				if (obj.has(ERROR_KEY)) {
					String msg = String.format("The %s command failed with error: %s", Arrays.toString(command), obj.getString(ERROR_DESCRIPTION_KEY)); //$NON-NLS-1$
					Logger.logWarning(msg);
					throw new IOException(msg);
				}
				if (obj.has(STATUS_KEY) && !STATUS_OK_VALUE.equals(obj.getString(STATUS_KEY))) {
					String msg = String.format("The %s command failed with error: %s", Arrays.toString(command), obj.getString(STATUS_MSG_KEY)); //$NON-NLS-1$
					Logger.logWarning(msg);
					throw new IOException(msg);
				}
			}
		} catch (JSONException e) {
			// Ignore
		}
	}

}
