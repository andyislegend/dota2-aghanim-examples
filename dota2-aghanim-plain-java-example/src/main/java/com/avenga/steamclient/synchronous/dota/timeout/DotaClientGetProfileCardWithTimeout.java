package com.avenga.steamclient.synchronous.dota.timeout;

import com.avenga.steamclient.enums.SteamGame;
import com.avenga.steamclient.steam.client.steamgamecoordinator.SteamGameCoordinator;
import com.avenga.steamclient.steam.client.steamgamecoordinator.dota.DotaClient;
import com.avenga.steamclient.steam.client.steamgameserver.SteamGameServer;
import com.avenga.steamclient.steam.client.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.client.steamuser.SteamUser;
import com.avenga.steamclient.steam.client.steamuser.UserLogOnResponse;
import com.avenga.steamclient.synchronous.client.SteamClientLogin;
import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Example show work of the callback handling with specified timeout for callback handling.
 * <p>
 * {@link DotaClient#getAccountProfileCard(int)} method during execution will register callbacks to {@link AbstractGameCoordinator}
 * callback queue and respective handlers will wait until message from Steam server will be received
 * or time for waiting callback exceed and handler will throw {@link CallbackTimeoutException}.
 */
public class DotaClientGetProfileCardWithTimeout {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SteamClientLogin.class);

    private static final long WAIT_CALLBACK_TIMEOUT = 10000;
    private static final int ACCOUNT_ID = 137935311;

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

        var steamUser = steamClient.getHandler(SteamUser.class);
        UserLogOnResponse logOnResponse;
        try {
            logOnResponse = steamUser.logOn(logOnDetails, WAIT_CALLBACK_TIMEOUT).get();
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
            var gameServer = steamClient.getHandler(SteamGameServer.class);
            var dotaClient = steamClient.getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);

            gameServer.setClientPlayedGame(List.of(SteamGame.Dota2.getApplicationId()), 15000L);
            dotaClient.sendClientHello(15000L);

            var profileCard = dotaClient.getAccountProfileCard(ACCOUNT_ID, WAIT_CALLBACK_TIMEOUT).get();
            LOGGER.info("Profile card: {}", profileCard.getAccountId());
        } catch (final CallbackTimeoutException e) {
            LOGGER.info("Account profile card response wasn't received: {}", e.getMessage());
            //We can use retry logic here, but for now we will just disconnect and stop execution.
            steamClient.disconnect();
            return;
        }

        // After we received all necessary information, we need logOff to close connection with Steam Network server.
        steamUser.logOff();
    }
}
