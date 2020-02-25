package com.avenga.steamclient.synchronous.dota.timeout;

import com.avenga.steamclient.synchronous.client.SteamClientLogin;
import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.coordinator.AbstractGameCoordinator;
import com.avenga.steamclient.steam.coordinator.impl.GameCoordinator;
import com.avenga.steamclient.steam.dota.impl.DotaClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.steam.steamuser.UserLogOnResponse;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example show work of the callback handling with specified timeout for callback handling.
 * <p>
 * {@link DotaClient#getMatchDetails(long)} method during execution will register callbacks to {@link AbstractGameCoordinator}
 * callback queue and respective handlers will wait until message from Steam server will be received
 * or time for waiting callback exceed and handler will throw {@link CallbackTimeoutException}.
 */
public class DotaClientGetMatchDetailsWithTimeout {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    private static final long WAIT_CALLBACK_TIMEOUT = 10000;
    private static final long MATCH_ID = 5239025268L;

    public static void main(String[] args) {
        // To check progress of the execution and sent/received packet from Steam Network we need DEBUG logger level
        LoggerUtils.initDebugLogger();

        var steamClient = new SteamClient();
        // We need open connection with one of the Steam Network servers which we will get from Steam Directory Web API endpoint
        try {
            steamClient.connect(WAIT_CALLBACK_TIMEOUT);
        } catch (final CallbackTimeoutException e) {
            LOGGER.info("Connection wasn't established with Steam Network during specified time.");
        }

        LOGGER.info("Connection was established with Steam Network.");

        var logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(args[0]);
        logOnDetails.setPassword(args[1]);

        var steamUser = new SteamUser(steamClient);
        UserLogOnResponse logOnResponse;
        try {
            logOnResponse = steamUser.logOn(logOnDetails, WAIT_CALLBACK_TIMEOUT);
            LOGGER.info("Status of the logOn request: {}", logOnResponse.getResult().name());
        } catch (final CallbackTimeoutException e) {
            LOGGER.info("LogOn response wasn't received during specified time.");
            //We can use retry logic here, but for now we will just disconnect and stop execution.
            steamClient.disconnect();
            return;
        }

        //We can use retry logic here, but for now we will just disconnect and stop execution.
        if (!logOnResponse.getResult().equals(EResult.OK)) {
            steamClient.disconnect();
        }

        // We need to create DOTA client to fetch data related to it from Game Coordinator server.
        try {
            var dotaClient = new DotaClient(new GameCoordinator(steamClient));
            var matchDetails = dotaClient.getMatchDetails(MATCH_ID, WAIT_CALLBACK_TIMEOUT);
            LOGGER.info("Match duration time: {}", matchDetails.getDuration());
        } catch (final CallbackTimeoutException e) {
            LOGGER.info("Match details response wasn't received: {}", e.getMessage());
            //We can use retry logic here, but for now we will just disconnect and stop execution.
            steamClient.disconnect();
            return;
        }

        // After we received all necessary information, we need logOff to close connection with Steam Network server.
        steamUser.logOff();
    }
}
