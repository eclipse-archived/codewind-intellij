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

import org.eclipse.codewind.intellij.core.FileUtil;
import org.eclipse.codewind.intellij.core.Logger;
import org.eclipse.codewind.intellij.core.PlatformUtil;
import org.eclipse.codewind.intellij.core.PlatformUtil.OperatingSystem;

public class CLIUtil {

    // Common options
    public static final String JSON_OPTION = "--json";
    public static final String CON_ID_OPTION = "--conid";

    private static final String INSTALLER_DIR = ".codewind-intellij";

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

    public static Process runCWCTL(String cmd, List<String> options) throws IOException {
        return runCWCTL(cmd, options.toArray(new String[0]));
    }

    public static Process runCWCTL(String cmd, String... options) throws IOException {
		return runCWCTL(null, new String[] {cmd}, options, null);
    }

	public static Process runCWCTL(String[] globalOptions, String[] cmd, String[] options) throws IOException {
		return runCWCTL(globalOptions, cmd, options, null);
    }

	public static Process runCWCTL(String[] globalOptions, String[] cmd, String[] options, String[] args) throws IOException {
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
        String[] command = cmdList.toArray(new String[cmdList.size()]);
        ProcessBuilder builder = new ProcessBuilder(command);
        if (PlatformUtil.getOS() == PlatformUtil.OperatingSystem.MAC) {
            String pathVar = System.getenv("PATH");
            pathVar = "/usr/local/bin:" + pathVar;
            Map<String, String> env = builder.environment();
            env.put("PATH", pathVar);
        }
        return builder.start();
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
        Path path = Paths.get(System.getProperty("user.home"), INSTALLER_DIR);
        return path.toString();
	}
	
	public static String[] getOptions(String[] options, String conid) {
		ArrayList<String> opts = new ArrayList<String>(Arrays.asList(options));
		if (conid != null) {
			opts.add(CON_ID_OPTION);
			opts.add(conid);
		}
		return opts.toArray(new String[opts.size()]);
    }
}
