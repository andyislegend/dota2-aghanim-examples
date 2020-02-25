package com.avenga.steamclient;

import com.avenga.steamclient.properties.SteamClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(SteamClientProperties.class)
public class SteamClientApplication {

    public static void main(String[] args) {
        SpringApplication.run(SteamClientApplication.class, args);
    }

}
