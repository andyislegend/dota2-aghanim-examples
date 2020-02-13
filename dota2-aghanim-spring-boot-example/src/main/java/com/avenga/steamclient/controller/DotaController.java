package com.avenga.steamclient.controller;

import com.avenga.steamclient.exception.CallbackTimeoutException;
import com.avenga.steamclient.model.AccountProfileCard;
import com.avenga.steamclient.model.MatchDetailsResponse;
import com.avenga.steamclient.properties.SteamClientProperties;
import com.avenga.steamclient.steam.dota.impl.DotaClient;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class DotaController {

    private final SteamClientProperties steamClientProperties;
    private final DotaClient dotaClient;

    @GetMapping("/dota/match/{matchId}/details")
    public MatchDetailsResponse getMatchDetails(@PathVariable long matchId) throws CallbackTimeoutException {
        return new MatchDetailsResponse(dotaClient.getMatchDetails(matchId, steamClientProperties.getCallbackWaitTimeout()));
    }

    @GetMapping("/dota/account/{accountId}/profile-card")
    public AccountProfileCard getProfileCard(@PathVariable int accountId) throws CallbackTimeoutException {
        return new AccountProfileCard(dotaClient.getAccountProfileCard(accountId, steamClientProperties.getCallbackWaitTimeout()));
    }
}
