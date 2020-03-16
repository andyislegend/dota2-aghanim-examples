package com.avenga.steamclient.config;

import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.properties.SteamClientProperties;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.client.steamgamecoordinator.SteamGameCoordinator;
import com.avenga.steamclient.steam.client.steamgamecoordinator.dota.DotaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class SteamClientConfig {

    private final SteamClientProperties steamClientProperties;

    @Bean(destroyMethod = "disconnect")
    public SteamClient steamClient() throws CallbackTimeoutException {
        return new SteamClient();
    }

    @Bean
    public DotaClient dotaClient() throws CallbackTimeoutException {
        return steamClient().getHandler(SteamGameCoordinator.class).getHandler(DotaClient.class);
    }
}
