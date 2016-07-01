package org.mockserver.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

import org.apache.commons.lang.StringEscapeUtils;

/**
 * author Valeriy Mironichev
 */
public class ResponsePayloadFieldValuePolicy extends ObjectWithReflectiveEqualsHashCodeToString {
    private String fieldName;
    private String fieldType;
    private String populateStrategy;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public ResponsePayloadFieldValuePolicy() {
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public String getPopulateStrategy() {
        return populateStrategy;
    }

    public void setPopulateStrategy(String populateStrategy) {
        this.populateStrategy = populateStrategy;
    }

    public void apply(String requestBody, HttpWebHookRequest webHook) {

        String webHookPayloadTemplate = webHook.getPayload();
        String body = webHook.getResponseBody();

        String escapedFieldName = "\\$\\{" + fieldName + "\\}";
        String value = null;
        // TODO(vmironichev): refactor to strategy classes
        if (webHookPayloadTemplate.contains(fieldName)) {
            switch (populateStrategy) {
            case "pass_from_request":
                value = getJsonAttributeValue(requestBody, fieldName);
                while (body.contains(StringEscapeUtils.unescapeJava(escapedFieldName))) {
                    body = body.replaceFirst(escapedFieldName, value);
                    webHook.setBody(body);
                }
                break;
            case "distribute_from_request":
                if ("decimal".equals(fieldType)) {
                    String fieldValue = getJsonAttributeValue(requestBody, fieldName);
                    if (isNumeric(fieldValue)) {
                        int count = countFieldOccurences(StringEscapeUtils.unescapeJava(escapedFieldName), webHookPayloadTemplate);
                        if (count > 0) {
                            body = populateDecimalValue(body, escapedFieldName, fieldValue, count);
                            webHook.setBody(body);
                        }
                    }
                }
                break;
            case "auto":
                while (body.contains(StringEscapeUtils.unescapeJava(escapedFieldName))) {
                    value = generateFieldValue(fieldType, value);
                    body = body.replaceFirst(escapedFieldName, value);
                }
                webHook.setBody(body);
                break;
            }
        }
    }

    private boolean isNumeric(String fieldValue) {
        return fieldValue.matches("-?\\d+(.\\d+)?");
    }

    private String populateDecimalValue(String body, String escapedFieldName, String fieldValue, int count) {

        BigDecimal newValue = new BigDecimal(fieldValue).divide(new BigDecimal(count)).setScale(2,
                RoundingMode.HALF_UP);
        while (body.contains(StringEscapeUtils.unescapeJava(escapedFieldName))) {
            body = body.replaceFirst(escapedFieldName, newValue.toString());
        }
        return body;
    }

    private String generateFieldValue(String fieldType, String value) {
        switch (fieldType) {
        case "uuid4":
            value = UUID.randomUUID().toString().replace("-", "");
            break;
        case "date":
            value = dateFormat.format(new Date());
            break;
        default:
            break;
        }
        return value;
    }

    private String getJsonAttributeValue(String requestBody, String fieldName) {
        String fieldValue = null;
        try {
            String fieldValueBegining = requestBody.substring(requestBody.lastIndexOf(fieldName));
            fieldValue = fieldValueBegining.substring(fieldValueBegining.indexOf(":") + 1,
                    fieldValueBegining.indexOf(","));
        } catch (Exception e) {
        }
        return fieldValue == null ? null : fieldValue.replace("\"", "").trim();
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
