package com.fallmerayer.radathina.api;

public class ApiRequestBuilder {
    private String protocol = "http";
    private String host = "localhost";
    private int port = 80;
    private String apiPath = "";

    public String build (String path, String... queryParameters) {
        String baseUrl = protocol + "://" + host + ":" + port + "/" + apiPath;

        String requestUrl = path;
        for (String queryParameter : queryParameters) {
            requestUrl += "&" + queryParameter;
        }

        return baseUrl + "/" + requestUrl;
    }

    public ApiRequestBuilder protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ApiRequestBuilder host(String host) {
        this.host = host;
        return this;
    }

    public ApiRequestBuilder port(int port) {
        this.port = port;
        return this;
    }

    public ApiRequestBuilder apiPath(String apiPath) {
        this.apiPath = apiPath;
        return this;
    }

}
