package com.avenga.steamclient.config;

import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.properties.SteamClientProperties;
import com.avenga.steamclient.steam.client.SteamClient;
import com.avenga.steamclient.steam.coordinator.impl.GameCoordinator;
import com.avenga.steamclient.steam.dota.impl.DotaClient;
import com.avenga.steamclient.steam.steamuser.LogOnDetails;
import com.avenga.steamclient.steam.steamuser.SteamUser;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

@Configuration
@RequiredArgsConstructor
public class SteamClientConfig {

    private final SteamClientProperties steamClientProperties;

    @Bean
    public SteamClient steamClient() throws CallbackTimeoutException {
        var steamClient = new SteamClient();
        steamClient.connect(steamClientProperties.getCallbackWaitTimeout());
        return steamClient;
    }

    @Bean(destroyMethod = "logOff")
    public SteamUser steamUser() throws CallbackTimeoutException {
        var steamUser = new SteamUser(steamClient());

        var logOnDetails = new LogOnDetails();
        logOnDetails.setUsername(steamClientProperties.getUsername());
        logOnDetails.setPassword(steamClientProperties.getPassword());
        steamUser.logOn(logOnDetails, steamClientProperties.getCallbackWaitTimeout());

        return steamUser;
    }

    @Bean
    @DependsOn("steamUser")
    public DotaClient dotaClient() throws CallbackTimeoutException {
        return new DotaClient(new GameCoordinator(steamClient()));
    }
}
