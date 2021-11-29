package com.quicandroid;

public class Server {
    private final String url;
    private final String implementation;

    public Server(String url, String implementation) {
        this.url = url;
        this.implementation = implementation;
    }

    public String getUrl() {
        return url;
    }

    public String getImplementation() {
        return implementation;
    }

    @Override
    public String toString() {
        return String.format("url: %s \nimplementation: %s", url, implementation);
    }
}
