package com.veracode.scamitigation.api;

import org.apache.sling.commons.json.JSONObject;

public class ApiResults {
    private final JSONObject apiResponse;

    public ApiResults(JSONObject apiResponse) {
        this.apiResponse = apiResponse;
    }

    public JSONObject getApiResponse() {
        return this.apiResponse;
    }

    public boolean hasResponse() {
        return this.apiResponse != null;
    }
}
