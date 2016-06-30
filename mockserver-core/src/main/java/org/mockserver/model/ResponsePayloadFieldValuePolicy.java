package org.mockserver.model;

/**
 * author Valeriy Mironichev
 */
public class ResponsePayloadFieldValuePolicy extends ObjectWithReflectiveEqualsHashCodeToString{
    private String fieldName;
    private String fieldType;
    private String populateStrategy;

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

}
