package com.quicandroid;

public class QuicRequest {
    private static native String quicRequest(final String pattern);

    public String sendQuicRequest(String to) {
        return quicRequest(to);
    }
}
