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

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class SteamClientConfig {
    private static final String CLIENT_LOGGER_NAME = "RegularSteamClient";
    private final SteamMultiClientProperties steamClientProperties;

    @Bean(destroyMethod = "disconnect")
    public SteamClient steamClient() throws CallbackTimeoutException {
        // To distinguish multiple steam client logs, we can provide custom client logger prefix.
        var steamClient = new SteamClient(CLIENT_LOGGER_NAME);
        // To use auto-reconnect functionality we need to register user credential provider with steam account credentials.
        steamClient.setCredentialsProvider(new UserCredentialsProvider(steamClientProperties.getLogOnDetails()));
        // To fetch data for Dota 2 from Steam Game Coordinator we need to init session after each auto-reconnect.
        steamClient.setOnAutoReconnect((client) -> {
            var gameServer = client.getHandler(SteamGameServer.class);
            var dotaClient = client.getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);
            try {
                gameServer.setClientPlayedGame(List.of(SteamGame.Dota2.getApplicationId()), steamClientProperties.getCallbackWaitTimeout());
                dotaClient.sendClientHello(steamClientProperties.getCallbackWaitTimeout());
            } catch (CallbackTimeoutException e) {
                // We set reconnectOnUserInitiated flag to re-init Game Coordinator session instead of closing connection.
                client.setReconnectOnUserInitiated(true);
                client.disconnect();
            }
            // We can get client name to use it for logging information in our business logic.
            log.info("{}: SUCCESSFULLY INIT GC SESSION", client.getClientName());
        });

        // To use socks proxy version 4 we need to set next system property. Java by default will use socks proxy version 5.
        System.setProperty("socksProxyVersion", "4");
        // We can provide amount of unsuccessful tries to open connection using proxy, before library will remove it from available proxies.
        steamClient.setMaxConnectionFialureCount(10);
        // We cam provide list of proxies which library will use to open connection to Steam Network.
        steamClient.registerConnectionProxies(List.of(
                Proxy.NO_PROXY
//                new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("1.53.137.12", 4145)),
//                new Proxy(Proxy.Type.HTTP, new InetSocketAddress("177.86.217.69", 53281))
        ));
        // After we configured auto-reconnect settings, we need to trigger establishing of the connection.
        steamClient.connectAndLogin();
        return steamClient;
    }

    @Bean
    public DotaClient dotaClient() throws CallbackTimeoutException {
        return steamClient().getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);
    }
}
