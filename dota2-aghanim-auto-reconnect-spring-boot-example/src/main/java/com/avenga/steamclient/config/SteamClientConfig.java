package com.avenga.steamclient.config;

import com.avenga.steamclient.enums.SteamGame;
import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.properties.SteamMultiClientProperties;
import com.avenga.steamclient.provider.UserCredentialsProvider;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.client.steamgamecoordinator.SteamGameCoordinator;
import com.avenga.steamclient.steam.client.steamgamecoordinator.dota.DotaClient;
import com.avenga.steamclient.steam.client.steamgameserver.SteamGameServer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SteamClientConfig {

    private final SteamMultiClientProperties steamClientProperties;

    @Bean
    public ExecutorService executorService() {
        return Executors.newWorkStealingPool();
    }

    @Bean(destroyMethod = "disconnect")
    public SteamClient steamClient() throws CallbackTimeoutException {
        var steamClient = new SteamClient();
        steamClient.setCredentialsProvider(new UserCredentialsProvider(steamClientProperties.getLogOnDetails()));
        steamClient.setOnAutoReconnect((client) -> {
            var gameServer = client.getHandler(SteamGameServer.class);
            var dotaClient = client.getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);
            try {
                gameServer.setClientPlayedGame(List.of(SteamGame.Dota2.getApplicationId()), steamClientProperties.getCallbackWaitTimeout());
                dotaClient.sendClientHello(steamClientProperties.getCallbackWaitTimeout());
            } catch (CallbackTimeoutException e) {
                client.setReconnectOnUserInitiated(true);
                client.disconnect();
            }
        });
        steamClient.connectAndLogin();
        return steamClient;
    }

    @Bean
    public DotaClient dotaClient() throws CallbackTimeoutException {
        return steamClient().getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);
    }
}
