package com.avenga.steamclient.asynchronous.dota;

import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.enums.SteamGame;
import com.avenga.steamclient.steam.asyncclient.SteamClientAsync;
import com.avenga.steamclient.steam.asyncclient.callbackmanager.DefaultCallbackManager;
import com.avenga.steamclient.steam.asyncclient.callbacks.ConnectedCallback;
import com.avenga.steamclient.steam.asyncclient.callbacks.DisconnectedCallback;
import com.avenga.steamclient.steam.asyncclient.steamgamecoordinator.SteamGameCoordinatorAsync;
import com.avenga.steamclient.steam.asyncclient.steamgamecoordinator.dota.DotaClientAsync;
import com.avenga.steamclient.steam.asyncclient.steamgamecoordinator.dota.callback.ClientWelcomeCallback;
import com.avenga.steamclient.steam.asyncclient.steamgamecoordinator.dota.callback.DotaMatchDetailsCallback;
import com.avenga.steamclient.steam.asyncclient.steamgameserver.SteamGameServerAsync;
import com.avenga.steamclient.steam.asyncclient.steamgameserver.callback.GameConnectTokensCallback;
import com.avenga.steamclient.steam.asyncclient.steamuser.SteamUserAsync;
import com.avenga.steamclient.steam.asyncclient.steamuser.callback.LoggedOffCallback;
import com.avenga.steamclient.steam.asyncclient.steamuser.callback.LoggedOnCallback;
import com.avenga.steamclient.steam.client.steamuser.LogOnDetails;
import com.avenga.steamclient.util.LoggerUtils;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Example show work of the callback handling using full asynchronous {@link SteamClientAsync} and {@link DotaClientAsync}.
 * {@link DefaultCallbackManager} provide possibility to subscribe on received packet messages from Steam Network and
 * Game Coordinator via correspond callbacks. Developer should provide implementation for handling client callbacks
 * for Game Coordinator services, e.g. {@link DotaMatchDetailsCallback}
 */
public class DotaClientGetMatchDetailsAsync implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(DotaClientGetMatchDetailsAsync.class);

    private static final long MATCH_ID = 5239025268L;

    private SteamClientAsync steamClient;

    private DefaultCallbackManager manager;

    private SteamUserAsync steamUser;

    private SteamGameServerAsync gameServerAsync;

    private DotaClientAsync dotaClientAsync;

    private boolean isRunning;

    private String username;

    private String password;

    private ObjectMapper mapper = new ObjectMapper();

    public DotaClientGetMatchDetailsAsync(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public static void main(String[] args) {
        // To check progress of the execution and sent/received packet from Steam Network we need DEBUG logger level
        LoggerUtils.initDebugLogger();

        if (args.length < 2) {
            LOGGER.debug("Sample1: No username and password specified!");
            return;
        }

        new DotaClientGetMatchDetailsAsync(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClientAsync();

        // create the callback manager which will route callbacks to function calls
        manager = new DefaultCallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUserAsync.class);
        gameServerAsync = steamClient.getHandler(SteamGameServerAsync.class);
        dotaClientAsync = steamClient.getHandler(SteamGameCoordinatorAsync.class).getHandler(DotaClientAsync.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        //Handle logOn and logOff response from Steam Network server.
        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

        //Handle initial opening communication with Game Coordinator.
        manager.subscribe(GameConnectTokensCallback.class, this::onGameTokens);
        manager.subscribe(ClientWelcomeCallback.class, this::onClientWelcome);

        //Handle response from Game Coordinator.
        manager.subscribe(DotaMatchDetailsCallback.class, this::onMatchDetails);

        isRunning = true;

        System.out.println("Connecting to steam...");

        // initiate the connection
        steamClient.connect();

        // create our callback handling loop
        while (isRunning) {
            // in order for the callbacks to get routed, they need to be handled by the manager
            manager.runWaitCallbacks(1000L);
        }
    }

    private void onConnected(ConnectedCallback callback) {
        System.out.println("Connected to Steam!");

        LogOnDetails details = new LogOnDetails();
        details.setUsername(username);
        details.setPassword(password);

        steamUser.logOn(details);
    }

    private void onDisconnected(DisconnectedCallback callback) {
        System.out.println("Disconnected from Steam");
        isRunning = false;
    }

    private void onLoggedOn(LoggedOnCallback callback) {
        if (callback.getResult() != EResult.OK) {
            if (callback.getResult() == EResult.AccountLogonDenied) {
                System.out.println("Unable to logon to Steam: This account is SteamGuard protected.");
                isRunning = false;
                return;
            }

            System.out.println("Unable to logon to Steam: " + callback.getResult());
            isRunning = false;
            return;

        }
        System.out.println("LogOn " + username + " to Steam: " + callback.getResult());
        gameServerAsync.sendGamePlayed(List.of(SteamGame.Dota2.getApplicationId()));
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());
        isRunning = false;
    }

    private void onGameTokens(GameConnectTokensCallback callback) {
        try {
            System.out.println("GameConnectTokens: " + System.lineSeparator() + mapper.writeValueAsString(callback));
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        dotaClientAsync.sendClientHello();
    }

    private void onClientWelcome(ClientWelcomeCallback callback) {
        try {
            System.out.println("ClientWelcome: " + System.lineSeparator() + mapper.writeValueAsString(callback));
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }

        dotaClientAsync.requestMatchDetails(MATCH_ID);
    }

    private void onMatchDetails(DotaMatchDetailsCallback callback) {
        try {
            System.out.println("MatchDetails: " + System.lineSeparator() + mapper.writeValueAsString(callback));
        } catch (JsonProcessingException e) {
            System.out.println(e.getMessage());
        }
        steamUser.logOff();
        isRunning = false;
    }
}
