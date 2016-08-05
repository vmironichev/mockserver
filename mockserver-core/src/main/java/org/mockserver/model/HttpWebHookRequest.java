package org.mockserver.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.mockserver.logging.LogFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * author Valeriy Mironichev
 */
public class HttpWebHookRequest extends ObjectWithReflectiveEqualsHashCodeToString {

    private static final String POST = "POST";
    private static final String ACCEPT = "Accept";
    private static final String CONTENT_TYPE = "Content-Type";
    private static final String APPLICATION_JSON = "application/json";

    private final Logger logger = LoggerFactory.getLogger(HttpWebHookRequest.class);
    private final LogFormatter logFormatter = new LogFormatter(logger);
    private static ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private String host;
    private Integer port;
    private String path;
    private String payload;
    private long executionDelay;
    private TimeUnit executionDelayTimeUnit;
    private String body;
    private Authentication authentication;

    public HttpWebHookRequest() {
    }

    public String getHost() {
        return host;
    }

    public HttpWebHookRequest setHost(String host) {
        this.host = host.startsWith("http") ? host : "http://" + host;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public HttpWebHookRequest setPort(Integer port) {
        this.port = port;
        return this;
    }

    public String getPath() {
        return path;
    }

    public HttpWebHookRequest setPath(String path) {
        this.path = path;
        return this;
    }

    public String getPayload() {
        return payload;
    }

    public HttpWebHookRequest setPayload(String payload) {
        this.payload = payload;
        return this;
    }

    public long getExecutionDelay() {
        return executionDelay;
    }

    public HttpWebHookRequest setExecutionDelay(long executionDelay) {
        this.executionDelay = executionDelay;
        return this;
    }

    public TimeUnit getExecutionDelayTimeUnit() {
        return executionDelayTimeUnit == null ? TimeUnit.MILLISECONDS : executionDelayTimeUnit;
    }

    public HttpWebHookRequest setExecutionDelayTimeUnit(TimeUnit executionDelayTimeUnit) {
        this.executionDelayTimeUnit = executionDelayTimeUnit;
        return this;
    }

    public void submit() {
        logFormatter.infoLog("Scheduling web hook for execution {}", this);
        executorService.schedule(webHookRequest(), getExecutionDelay(), getExecutionDelayTimeUnit());
    }

    private Runnable webHookRequest() {

        return new Runnable() {

            @Override
            public void run() {

                try {
                    HttpURLConnection httpRequest = createRequest();
                    if (authentication != null) {
                        authentication.sign(httpRequest);
                    }
                    String responseBody = getResponseBody();
                    if (responseBody != null) {
                        byte[] data = responseBody.getBytes("UTF-8");
                        httpRequest.setRequestMethod(POST);
                        httpRequest.setFixedLengthStreamingMode(data.length);
                        httpRequest.setDoOutput(true);
                        httpRequest.getOutputStream().write(data);
                    }
                    httpRequest.connect();
                    handleResponse(httpRequest);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }

        };
    }

    private void handleResponse(HttpURLConnection httpRequest) throws IOException {

        BufferedReader in = new BufferedReader(new InputStreamReader(httpRequest.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        System.out.println(response.toString());
        logFormatter.infoLog("Web hook executed, response recevied: {}" + System.getProperty("line.separator") + " for request:{}", response, httpRequest);
    }

    private HttpURLConnection createRequest() throws IOException, AuthenticationException {
        URL url = new URL(host + (port != null ? ":" + port : "") + path);
        HttpURLConnection httpRequest = (HttpURLConnection) url.openConnection();
        httpRequest.setRequestProperty(ACCEPT, APPLICATION_JSON);
        httpRequest.setRequestProperty(CONTENT_TYPE, APPLICATION_JSON);
        return httpRequest;
    }

    public void setBody(String body) {
        this.body = body;
    }
    
    public String getResponseBody() {
        return body == null ? payload : body;
    }

    public void setAuthentication(Authentication authentication) {
        this.authentication = authentication;
    }

    public Authentication getAuthentication() {
        return authentication;
    }
}
