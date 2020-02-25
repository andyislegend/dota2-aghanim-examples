package com.avenga.steamclient.synchronous.dota;

import com.avenga.steamclient.synchronous.client.SteamClientLogin;
import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.coordinator.AbstractGameCoordinator;
import com.avenga.steamclient.steam.coordinator.impl.GameCoordinator;
import com.avenga.steamclient.steam.dota.impl.DotaClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.util.LoggerUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Example show work of the callback handling using {@link java.util.concurrent.CompletableFuture}
 * returned after request was send to Steam Network and Game Coordinator server.
 * <p>
 * {@link DotaClient#getAccountProfileCard(int)} method during execution will register callbacks to {@link AbstractGameCoordinator}
 * callback queue. Method will return registered to queue CompletableFuture with applied proto buffer message
 * to correspond pojo class transformer.
 */
public class DotaClientGetProfileCard {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    private static final int ACCOUNT_ID = 137935311;
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

        //We can use retry logic here, but for now we will just disconnect and stop execution.
        if (!logOnResponse.getResult().equals(EResult.OK)) {
            steamClient.disconnect();
        }

        // We need to create DOTA client to fetch data related to it from Game Coordinator server.
        try {
            var dotaClient = new DotaClient(new GameCoordinator(steamClient));
            var profileCard = dotaClient.getAccountProfileCard(ACCOUNT_ID).thenApply((dotaProfileCard) -> {
                LOGGER.info("Profile card: {}", dotaProfileCard.getAccountId());
                return dotaProfileCard;
            });
            profileCard.get(DEFAULT_CALLBACK_TIMEOUT, TimeUnit.SECONDS);
        } catch (final CallbackTimeoutException | TimeoutException e) {
            LOGGER.info("Account profile card response wasn't received: {}", e.getMessage());
            //We can use retry logic here, but for now we will just disconnect and stop execution.
            steamUser.logOff();
            return;
        }

        // After we received all necessary information, we need logOff to close connection with Steam Network server.
        steamUser.logOff();
    }
}
