package com.avenga.steamclient.dota;

import com.avenga.steamclient.client.SteamClientLogin;
import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.coordinator.AbstractGameCoordinator;
import com.avenga.steamclient.steam.coordinator.impl.GameCoordinator;
import com.avenga.steamclient.steam.dota.impl.DotaClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example show work of the callback handling without timeout.
 * <p>
 * {@link DotaClient#getMatchDetails(long)} method during execution will register callbacks to {@link AbstractGameCoordinator}
 * callback queue and respective handlers will wait until message from Steam server will be received and callback
 * will be completed and proto buffer message will be extracted to correspond response class.
 */
public class DotaClientGetMatchDetails {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    private static final long MATCH_ID = 5239025268L;

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

        //We can use retry logic here, but for now we will just disconnect and stop execution.
        if (!logOnResponse.getResult().equals(EResult.OK)) {
            steamClient.disconnect();
        }

        // We need to create DOTA client to fetch data related to it from Game Coordinator server.
        var dotaClient = new DotaClient(new GameCoordinator(steamClient));
        var matchDetails = dotaClient.getMatchDetails(MATCH_ID);
        LOGGER.info("Match duration time: {}", matchDetails.getMatch().getDuration());

        // After we received all necessary information, we need logOff to close connection with Steam Network server.
        steamUser.logOff();
    }
}
