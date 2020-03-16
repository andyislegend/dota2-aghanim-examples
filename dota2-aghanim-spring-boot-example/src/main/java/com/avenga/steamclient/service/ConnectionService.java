package com.avenga.steamclient.service;

import com.avenga.steamclient.exception.CallbackTimeoutException;

public interface ConnectionService {

    void connectAndInitGCSession() throws CallbackTimeoutException, InterruptedException;

    void incrementFailedRequestCount();
}
