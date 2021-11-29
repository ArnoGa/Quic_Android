package com.quicandroid;

public class Server {
    private final String url;
    private final String implementation;
    private int result;

    public Server(String url, String implementation) {
        this.url = url;
        this.implementation = implementation;
        this.result = 0;
    }

    public String getUrl() {
        return url;
    }

    public String getImplementation() {
        return implementation;
    }

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return String.format("url: %s \nimplementation: %s", url, implementation);
    }
}
