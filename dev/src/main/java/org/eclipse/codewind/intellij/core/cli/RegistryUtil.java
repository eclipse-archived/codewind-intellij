package org.eclipse.codewind.intellij.core.cli;

import com.intellij.openapi.progress.ProgressIndicator;
import org.eclipse.codewind.intellij.core.ProcessHelper;
import org.eclipse.codewind.intellij.core.connection.RegistryInfo;
import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeoutException;

public class RegistryUtil {
    private static final String REGISTRYSECRETS_CMD = "registrysecrets";
    private static final String[] REG_SECRET_LIST_CMD = new String[] {REGISTRYSECRETS_CMD, "list"};
    private static final String[] REG_SECRET_ADD_CMD = new String[] {REGISTRYSECRETS_CMD, "add"};
    private static final String[] REG_SECRET_REMOVE_CMD = new String[] {REGISTRYSECRETS_CMD, "remove"};

    private static final String ADDRESS_OPTION = "--address";
    private static final String USERNAME_OPTION = "--username";
    private static final String PASSWORD_OPTION = "--password";

    public static List<RegistryInfo> listRegistrySecrets(String conid, ProgressIndicator mon) throws IOException, JSONException, TimeoutException {
        Process process = null;
        try {
            process = CLIUtil.runCWCTL(CLIUtil.GLOBAL_JSON_INSECURE, REG_SECRET_LIST_CMD, new String[] {CLIUtil.CON_ID_OPTION, conid});
            ProcessHelper.ProcessResult result = ProcessHelper.waitForProcess(process, 500, 60);
            CLIUtil.checkResult(REG_SECRET_LIST_CMD, result, true);
            JSONArray registryArray = new JSONArray(result.getOutput().trim());
            List<RegistryInfo> registries = new ArrayList();
            for (int i = 0; i < registryArray.length(); i++) {
                registries.add(new RegistryInfo(registryArray.getJSONObject(i)));
            }
            return registries;
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }

    public static void addRegistrySecret(String address, String username, String password, String conid, ProgressIndicator monitor) throws IOException, JSONException, TimeoutException {
        runRegistrySecretCmd(REG_SECRET_ADD_CMD, new String[] {ADDRESS_OPTION, address, USERNAME_OPTION, username, PASSWORD_OPTION, password, CLIUtil.CON_ID_OPTION, conid}, null, monitor);
    }

    public static void removeRegistrySecret(String address, String conid, ProgressIndicator monitor) throws IOException, JSONException, TimeoutException {
        runRegistrySecretCmd(REG_SECRET_REMOVE_CMD, new String[] {ADDRESS_OPTION, address, CLIUtil.CON_ID_OPTION, conid}, null, monitor);
    }

    private static void runRegistrySecretCmd(String[] command, String[] options, String[] args, ProgressIndicator mon) throws IOException, JSONException, TimeoutException {
        Process process = null;
        try {
            process = CLIUtil.runCWCTL(CLIUtil.GLOBAL_JSON_INSECURE, command, options, args);
            ProcessHelper.ProcessResult result = ProcessHelper.waitForProcess(process, 500, 60);
            CLIUtil.checkResult(command, result, false);
        } finally {
            if (process != null && process.isAlive()) {
                process.destroy();
            }
        }
    }
}
