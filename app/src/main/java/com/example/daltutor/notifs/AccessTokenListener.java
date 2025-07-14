package com.example.daltutor.notifs;

public interface AccessTokenListener {
    void onAccessTokenReceived(String token);
    void onAccessTokenError(Exception exception);
}

