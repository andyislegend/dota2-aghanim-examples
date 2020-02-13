package com.avenga.steamclient.client;

import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example show work of the callback handling without timeout.
 * <p>
 * {@link SteamClient#connect()} and {@link SteamUser#logOn(LogOnDetails)} methods during execution will register callbacks
 * to {@link SteamClient} callback queue and respective handlers will wait until message from Steam server will be received
 * and callback will be completed and proto buffer message will be extracted to correspond response class.
 */
public class SteamClientLogin {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    public static void main(String[] args) {
        // To check progress of the execution and sent/received packet from Steam Network we need DEBUG logger level
        LoggerUtils.initDebugLogger();

        var steamClient = new SteamClient();
        // We need open connection with one of the Steam Network servers which we will get from Steam Directory Web API endpoint
        steamClient.connect();
        LOGGER.info("Connection was established with Steam Network.");

        var logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(args[0]);
        logOnDetails.setPassword(args[1]);

        var steamUser = new SteamUser(steamClient);
        var logOnResponse = steamUser.logOn(logOnDetails);
        LOGGER.info("Status of the logOn request: {}", logOnResponse.getResult().name());

        // We need to close connection opened with Steam Network server
        if (logOnResponse.getResult().equals(EResult.OK)) {
            steamUser.logOff();
        } else {
            steamClient.disconnect();
        }
    }
}
