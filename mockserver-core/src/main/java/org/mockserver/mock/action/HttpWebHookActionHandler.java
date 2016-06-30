package org.mockserver.mock.action;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;
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

            List<HttpWebHookRequest> requests = webHookConfig.getEndpoints();
            List<ResponsePayloadFieldValuePolicy> valuePolicies = webHookConfig.getResponsePayloadFieldValuePolicies();
            if (valuePolicies != null) {
                applyFieldValuePolicies(httpRequest, valuePolicies, requests);
            }

            for (HttpWebHookRequest request : requests) {
                request.submit();
            }
        }
        return httpWebHook.getHttpResponse().shallowClone();
    }

    private List<HttpWebHookRequest> applyFieldValuePolicies(HttpRequest httpRequest,
            List<ResponsePayloadFieldValuePolicy> valuePolicies, List<HttpWebHookRequest> webHookRequests) {

        String requestBody = httpRequest.getBodyAsString();

        List<HttpWebHookRequest> webHooks = new ArrayList<HttpWebHookRequest>();

        for (ResponsePayloadFieldValuePolicy policy : valuePolicies) {
            String strategy = policy.getPopulateStrategy();
            String fieldName = policy.getFieldName();
            String fieldType = policy.getFieldType();

            String escapedFieldName = "\\$\\{" + fieldName + "\\}";

            switch (strategy) {
            case "distribute_from_request":

                if ("decimal".equals(fieldType)) {
                    String fieldValue = getFieldValue(requestBody, fieldName);
                    if (fieldValue != null) {
                        BigDecimal decimalValue = null;
                        try {
                            decimalValue = new BigDecimal(fieldValue);
                            for (HttpWebHookRequest request : webHookRequests) {
                                String requestPayload = request.getPayload();
                                String unescapedFieldName = StringEscapeUtils.unescapeJava(escapedFieldName);
                                int count = countFieldOccurences(unescapedFieldName, requestPayload);
                                if (count > 0) {
                                    String payload = requestPayload.replace(unescapedFieldName,
                                            decimalValue.divide(new BigDecimal(count)).toString());
                                    request.setPayload(payload);
                                }
                            }
                        } catch (NumberFormatException e) {
                        }
                    }
                }
                break;
            case "auto":

                if ("uuid4".equals(fieldType)) {
                    for (HttpWebHookRequest request : webHookRequests) {
                        String requestPayload = request.getPayload();
                        String payload = requestPayload.replaceFirst(escapedFieldName,
                                UUID.randomUUID().toString().replace("-", ""));
                        request.setPayload(payload);
                    }
                }
                break;
            default:
                break;
            }
        }

        return webHooks;
    }

    private String getFieldValue(String requestBody, String fieldName) {
        String fieldValue = null;
        try {
            String fieldValueBegining = requestBody.substring(requestBody.lastIndexOf(fieldName));
            fieldValue = fieldValueBegining.substring(fieldValueBegining.indexOf(":") + 1,
                    fieldValueBegining.indexOf(","));
        } catch (Exception e) {
        }
        return fieldValue == null ? null : fieldValue.trim();
    }

    private int countFieldOccurences(String toFind, String inString) {
        int count = 0;
        int lastIndex = 0;
        while (lastIndex != -1) {
            lastIndex = inString.indexOf(toFind, lastIndex);
            if (lastIndex != -1) {
                count++;
                lastIndex += toFind.length();
            }
        }
        return count;
    }

}
