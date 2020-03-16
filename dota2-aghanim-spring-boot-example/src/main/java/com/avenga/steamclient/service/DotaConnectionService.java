package com.avenga.steamclient.service;

import com.avenga.steamclient.enums.EResult;
import com.avenga.steamclient.enums.SteamGame;
import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.exception.InvalidUserException;
import com.avenga.steamclient.exception.RateLimitExceedException;
import com.avenga.steamclient.properties.SteamClientProperties;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.client.steamgamecoordinator.SteamGameCoordinator;
import com.avenga.steamclient.steam.client.steamgamecoordinator.dota.DotaClient;
import com.avenga.steamclient.steam.client.steamgameserver.SteamGameServer;
import com.avenga.steamclient.steam.client.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.client.steamuser.SteamUser;
import com.avenga.steamclient.steam.client.steamuser.UserLogOnResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@RequiredArgsConstructor
public class DotaConnectionService implements ConnectionService {

    private static final long INITIAL_DELAY = 20;
    private static final long PERIOD_DELAY = 10;
    private static final int FAILED_REQUESTS_LIMIT = 5;

    private final SteamClientProperties clientProperties;
    private final SteamClient steamClient;
    private final ScheduledExecutorService executorService;

    private AtomicInteger failedRequestCount = new AtomicInteger();

    @PostConstruct
    public void connect() throws CallbackTimeoutException {
        connectAndInitGCSession();
        executorService.scheduleAtFixedRate(() -> {
            log.debug("Checking status of the connection to Steam Network");
            if (!steamClient.isConnected() || failedRequestCount.get() >= FAILED_REQUESTS_LIMIT) {
                try {
                    connectAndInitGCSession();
                } catch (CallbackTimeoutException | RateLimitExceedException e) {
                    log.warn("Failed to reconnect to Steam Network due to: {}", e.getMessage());
                }
            }
        }, INITIAL_DELAY, PERIOD_DELAY, TimeUnit.SECONDS);
    }

    @Override
    public void connectAndInitGCSession() throws CallbackTimeoutException {
        var steamUser = steamClient.getHandler(SteamUser.class);
        var gameServer = steamClient.getHandler(SteamGameServer.class);
        var dotaClient = steamClient.getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);


        var logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(clientProperties.getUsername());
        logOnDetails.setPassword(clientProperties.getPassword());
        steamClient.connect(clientProperties.getCallbackWaitTimeout());
        var logOnResponse = steamUser.logOn(logOnDetails, clientProperties.getCallbackWaitTimeout());

        gameServer.setClientPlayedGame(List.of(SteamGame.Dota2.getApplicationId()), clientProperties.getCallbackWaitTimeout());
        dotaClient.sendClientHello(clientProperties.getCallbackWaitTimeout());
    }

    @Override
    public void incrementFailedRequestCount() {
        failedRequestCount.incrementAndGet();
    }

    private void checkLogOnResult(Optional<UserLogOnResponse> logOnResponse, LogOnDetails logOnDetails) throws CallbackTimeoutException {
        var steamUser = steamClient.getHandler(SteamUser.class);

        if (logOnResponse.isPresent() && logOnResponse.get().getResult() != EResult.OK) {
            var logOnResult = logOnResponse.get().getResult();
            if (logOnResult == EResult.AccessDenied || logOnResult == EResult.InvalidPassword) {
                throw new InvalidUserException("User can't be logged due to: " + logOnResult);
            }
            if (logOnResult == EResult.NoConnection || logOnResult == EResult.ServiceUnavailable
                    || logOnResult == EResult.Timeout || logOnResult == EResult.TryAnotherCM) {
                try {
                    TimeUnit.MILLISECONDS.sleep(5000);
                } catch (InterruptedException e) {
                    log.info("Timeout was interrupted");
                }
                steamClient.connect(clientProperties.getCallbackWaitTimeout());
                checkLogOnResult(steamUser.logOn(logOnDetails, clientProperties.getCallbackWaitTimeout()), logOnDetails);
            }
            if (logOnResult == EResult.RateLimitExceeded) {
                throw new RateLimitExceedException("LogOn rate limit is exceeded");
            }
        }
    }
}
