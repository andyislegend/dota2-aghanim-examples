package com.avenga.steamclient;

import com.avenga.steamclient.properties.SteamMultiClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SteamMultiClientProperties.class})
public class SteamClientAutoReconnectApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamClientAutoReconnectApplication.class, args);
    }
}
