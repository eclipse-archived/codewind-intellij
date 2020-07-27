package org.eclipse.codewind.intellij.ui.wizard;

import org.eclipse.codewind.intellij.core.connection.RegistryInfo;

public class RegistryTableEntry {
    public final String address;
    public String namespace;
    public final String username;
    public final String password;
    public boolean isPushReg = false;

    public RegistryTableEntry(String address, String namespace, String username, String password, boolean isPush) {
        this.address = address;
        this.namespace = namespace;
        this.username = username;
        this.password = password;
        this.isPushReg = isPush;
    }

    public RegistryTableEntry(RegistryInfo info) {
        this.address = info.getAddress();
        this.username = info.getUsername();
        this.password = null;
    }
}
