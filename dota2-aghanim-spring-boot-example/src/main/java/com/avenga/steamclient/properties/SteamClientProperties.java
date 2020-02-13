package com.avenga.steamclient.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@ConfigurationProperties(prefix = "steam-client")
public class SteamClientProperties {
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    private long callbackWaitTimeout = 30000;
}
