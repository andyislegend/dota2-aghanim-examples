package com.avenga.steamclient.asynchronous.client;

import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.steam.asyncclient.SteamClientAsync;
import com.avenga.steamclient.steam.asyncclient.callbackmanager.DefaultCallbackManager;
import com.avenga.steamclient.steam.asyncclient.callbacks.ConnectedCallback;
import com.avenga.steamclient.steam.asyncclient.callbacks.DisconnectedCallback;
import com.avenga.steamclient.steam.asyncclient.steamuser.SteamUserAsync;
import com.avenga.steamclient.steam.asyncclient.steamuser.callback.LoggedOffCallback;
import com.avenga.steamclient.steam.asyncclient.steamuser.callback.LoggedOnCallback;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.util.LoggerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Example shows work of the callback handling using full asynchronous {@link SteamClientAsync}. {@link DefaultCallbackManager}
 * provide possibility to subscribe on received packet messages from Steam Network via correspond callbacks.
 * Developer should provide implementation for handling client callbacks for Steam services, e.g. {@link LoggedOnCallback}
 */
public class SteamClientLoginAsync implements Runnable {

    private static final Logger LOGGER = LoggerFactory.getLogger(SteamClientLoginAsync.class);

    private SteamClientAsync steamClient;

    private DefaultCallbackManager manager;

    private SteamUserAsync steamUser;

    private boolean isRunning;

    private String username;

    private String password;

    public SteamClientLoginAsync(String username, String password) {
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

        new SteamClientLoginAsync(args[0], args[1]).run();
    }

    @Override
    public void run() {
        // create our steamclient instance
        steamClient = new SteamClientAsync();

        // create the callback manager which will route callbacks to function calls
        manager = new DefaultCallbackManager(steamClient);

        // get the steamuser handler, which is used for logging on after successfully connecting
        steamUser = steamClient.getHandler(SteamUserAsync.class);

        // register a few callbacks we're interested in
        // these are registered upon creation to a callback manager, which will then route the callbacks
        // to the functions specified
        manager.subscribe(ConnectedCallback.class, this::onConnected);
        manager.subscribe(DisconnectedCallback.class, this::onDisconnected);

        //Handle logOn and logOff response from Steam Network server.
        manager.subscribe(LoggedOnCallback.class, this::onLoggedOn);
        manager.subscribe(LoggedOffCallback.class, this::onLoggedOff);

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
        steamUser.logOff();
    }

    private void onLoggedOff(LoggedOffCallback callback) {
        System.out.println("Logged off of Steam: " + callback.getResult());
        isRunning = false;
    }
}
