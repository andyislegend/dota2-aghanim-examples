package com.avenga.steamclient.synchronous.client;

import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Example show work of the callback handling using {@link java.util.concurrent.CompletableFuture}
 * returned after request was send to Steam Network server.
 * <p>
 * {@link SteamClient#connect()} and {@link SteamUser#logOn(LogOnDetails)} methods during execution will register callbacks
 * to {@link SteamClient} callback queue. Method will return registered to queue CompletableFuture
 * with applied proto buffer message to correspond pojo class transformer.
 */
public class SteamClientLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);
    private static final long DEFAULT_CALLBACK_TIMEOUT = 10;

    public static void main(String[] args) throws Exception {
        // To check progress of the execution and sent/received packet from Steam Network we need DEBUG logger level
        LoggerUtils.initDebugLogger();

        var steamClient = new SteamClient();
        // We need open connection with one of the Steam Network servers which we will get from Steam Directory Web API endpoint
        var connectCallback = steamClient.connectAndGetCallback()
                .thenAccept((packetMessage) -> LOGGER.info("Connection was established with Steam Network."));
        connectCallback.get(DEFAULT_CALLBACK_TIMEOUT, TimeUnit.SECONDS);

        var logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(args[0]);
        logOnDetails.setPassword(args[1]);

        var steamUser = new SteamUser(steamClient);
        var logOnCallback = steamUser.logOn(logOnDetails)
                .thenApply((userLogOnResponse) -> {
                    LOGGER.info("Status of the logOn request: {}", userLogOnResponse.getResult().name());
                    return userLogOnResponse;
                });
        var logOnResponse = logOnCallback.get(DEFAULT_CALLBACK_TIMEOUT, TimeUnit.SECONDS);

        // We need to close connection opened with Steam Network server
        if (logOnResponse.getResult().equals(EResult.OK)) {
            steamUser.logOff();
        } else {
            steamClient.disconnect();
        }
    }
}
