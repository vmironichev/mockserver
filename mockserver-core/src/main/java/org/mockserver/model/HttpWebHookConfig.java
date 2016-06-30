package org.mockserver.model;

import java.util.List;

/**
 * author Valeriy Mironichev
 */
public class HttpWebHookConfig {
    private List<HttpWebHookRequest> endpoints;
    private List<ResponsePayloadFieldValuePolicy> responsePayloadFieldValuePolicies;

    public HttpWebHookConfig() {
    }

    public List<HttpWebHookRequest> getEndpoints() {
        return endpoints;
    }

    public void setEndpoints(List<HttpWebHookRequest> endpoints) {
        this.endpoints = endpoints;
    }

    public List<ResponsePayloadFieldValuePolicy> getResponsePayloadFieldValuePolicies() {
        return responsePayloadFieldValuePolicies;
    }

    public void setResponsePayloadFieldValuePolicies(
            List<ResponsePayloadFieldValuePolicy> responsePayloadFieldValuePolicies) {
        this.responsePayloadFieldValuePolicies = responsePayloadFieldValuePolicies;
    }

}
