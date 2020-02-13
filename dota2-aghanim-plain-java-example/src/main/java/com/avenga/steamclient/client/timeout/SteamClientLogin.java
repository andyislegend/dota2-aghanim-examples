package com.avenga.steamclient.client.timeout;

import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.steam.steamuser.UserLogOnResponse;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example show work of the callback handling with specified timeout for callback handling.
 * <p>
 * {@link SteamClient#connect()} and {@link SteamUser#logOn(LogOnDetails)} methods during execution will register callbacks
 * to {@link SteamClient} callback queue and respective handlers will wait until message from Steam server will be received
 * or time for waiting callback exceed and handler will throw {@link CallbackTimeoutException}.
 */
public class SteamClientLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    private static final long WAIT_CALLBACK_TIMEOUT = 10000;

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

        steamUser.logOff();
    }
}
