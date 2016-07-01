package org.mockserver.mock.action;

import java.util.List;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.HttpWebHook;
import org.mockserver.model.HttpWebHookConfig;
import org.mockserver.model.HttpWebHookRequest;
import org.mockserver.model.ResponsePayloadFieldValuePolicy;

/**
 * author Valeriy Mironichev
 */
public class HttpWebHookActionHandler extends HttpResponseActionHandler {

    public HttpResponse handle(HttpWebHook httpWebHook, HttpRequest httpRequest) {

        HttpWebHookConfig webHookConfig = httpWebHook.getHttpWebHookConfig();
        if (webHookConfig != null) {
            String requestBody = httpRequest.getBodyAsString();
            List<HttpWebHookRequest> requests = webHookConfig.getEndpoints();
            List<ResponsePayloadFieldValuePolicy> valuePolicies = webHookConfig.getResponsePayloadFieldValuePolicies();
            for (HttpWebHookRequest webHookRequest : requests) {
                webHookRequest.setBody(webHookRequest.getPayload());
                applyFieldValuePolicies(requestBody, valuePolicies, webHookRequest);
                webHookRequest.submit();
            }
        }
        return httpWebHook.getHttpResponse().shallowClone();
    }

    private void applyFieldValuePolicies(String requestBody, List<ResponsePayloadFieldValuePolicy> valuePolicies,
            HttpWebHookRequest webHookRequest) {
        if (valuePolicies != null) {
            for (ResponsePayloadFieldValuePolicy policy : valuePolicies) {
                policy.apply(requestBody, webHookRequest);
            }
        }
    }

}
