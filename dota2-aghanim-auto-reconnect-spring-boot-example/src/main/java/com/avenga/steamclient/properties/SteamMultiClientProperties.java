package com.avenga.steamclient.properties;

import com.avenga.steamclient.steam.client.steamuser.LogOnDetails;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
@ConfigurationProperties(prefix = "steam-multi-client")
public class SteamMultiClientProperties {
    private List<LogOnDetails> logOnDetails;
    private long callbackWaitTimeout = 15000;
}
