package com.fallmerayer.radathina.api.core;

public class ApiClientOptions {
    protected String protocol = "http";
    protected String host = "localhost";
    protected int port = 80;
    protected String apiPath = "";

    public ApiClientOptions protocol(String protocol) {
        this.protocol = protocol;
        return this;
    }

    public ApiClientOptions host(String host) {
        this.host = host;
        return this;
    }

    public ApiClientOptions port(int port) {
        this.port = port;
        return this;
    }

    public ApiClientOptions apiPath(String apiPath) {
        this.apiPath = apiPath;
        return this;
    }
}
