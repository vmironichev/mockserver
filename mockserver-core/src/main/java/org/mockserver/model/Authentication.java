package org.mockserver.model;

import java.net.HttpURLConnection;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.http.HttpParameters;

public class Authentication extends ObjectWithReflectiveEqualsHashCodeToString {

    private String type;
    private String realm;
    private String consumerKey;
    private String consumerSecret;
    private String token;
    private String tokenSecret;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    public String getConsumerKey() {
        return consumerKey;
    }

    public void setConsumerKey(String consumerKey) {
        this.consumerKey = consumerKey;
    }

    public String getConsumerSecret() {
        return consumerSecret;
    }

    public void setConsumerSecret(String consumerSecret) {
        this.consumerSecret = consumerSecret;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTokenSecret() {
        return tokenSecret;
    }

    public void setTokenSecret(String tokenSecret) {
        this.tokenSecret = tokenSecret;
    }

    public void sign(HttpURLConnection httpRequest) throws AuthenticationException {

        OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);
        consumer.setTokenWithSecret(token, tokenSecret);
        HttpParameters httpParameters = new HttpParameters();
        httpParameters.put("realm", realm);
       
        consumer.setAdditionalParameters(httpParameters);

        try {
            consumer.sign(httpRequest);
        } catch (Exception e) {
            throw new AuthenticationException("Could not sign http request. " + e.getMessage(), e);
        }
    }

}
