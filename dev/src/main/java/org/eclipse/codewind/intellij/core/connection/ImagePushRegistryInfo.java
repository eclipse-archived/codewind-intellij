package org.eclipse.codewind.intellij.core.connection;

import org.json.JSONObject;

public class ImagePushRegistryInfo  extends JSONObjectResult {

    public static final String ADDRESS_KEY = "address";
    public static final String NAMESPACE_KEY = "namespace";

    public ImagePushRegistryInfo(JSONObject obj) {
        super(obj, "image push registry");
    }

    public String getAddress() {
        return getString(ADDRESS_KEY);
    }

    public String getNamespace() {
        return getString(NAMESPACE_KEY);
    }
}
