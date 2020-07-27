package org.eclipse.codewind.intellij.core.connection;

import org.json.JSONObject;

public class RegistryInfo extends JSONObjectResult {

    public static final String ADDRESS_KEY = "address";
    public static final String USERNAME_KEY = "username";

    public RegistryInfo(JSONObject obj) {
        super(obj, "registry");
    }

    public String getAddress() {
        return getString(ADDRESS_KEY);
    }

    public String getUsername() {
        return getString(USERNAME_KEY);
    }
}
