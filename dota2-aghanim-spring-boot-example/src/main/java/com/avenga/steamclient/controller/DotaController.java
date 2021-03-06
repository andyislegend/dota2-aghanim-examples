package com.avenga.steamclient.controller;

import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.model.steam.gamecoordinator.dota.account.DotaProfileCard;
import com.avenga.steamclient.model.steam.gamecoordinator.dota.match.DotaMatchDetails;
import com.avenga.steamclient.properties.SteamClientProperties;
import com.avenga.steamclient.service.ConnectionService;
import com.avenga.steamclient.steam.client.steamgamecoordinator.dota.DotaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DotaController {

    private final SteamClientProperties steamClientProperties;
    private final DotaClient dotaClient;
    private final ConnectionService connectionService;

    @GetMapping("/dota/match/{matchId}/details")
    public DotaMatchDetails getMatchDetails(@PathVariable long matchId) throws CallbackTimeoutException {
        try {
            return dotaClient.getMatchDetails(matchId, steamClientProperties.getCallbackWaitTimeout()).get();
        } catch (CallbackTimeoutException e) {
            connectionService.incrementFailedRequestCount();
            throw e;
        }
    }

    @GetMapping("/dota/account/{accountId}/profile-card")
    public DotaProfileCard getProfileCard(@PathVariable int accountId) throws CallbackTimeoutException {
        try {
            return dotaClient.getAccountProfileCard(accountId, steamClientProperties.getCallbackWaitTimeout()).get();
        } catch (CallbackTimeoutException e) {
            connectionService.incrementFailedRequestCount();
            throw e;
        }
    }
}
