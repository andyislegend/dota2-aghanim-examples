package com.avenga.steamclient.model;

import com.avenga.steamclient.protobufs.dota.DotaGCMessagesClient.CMsgGCMatchDetailsResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
public class MatchDetailsResponse {

    private int result;
    private DotaMatch match;


    public MatchDetailsResponse(CMsgGCMatchDetailsResponse response) {
        this.result = response.getResult();
        this.match = DotaMatch.builder()
                .duration(response.getMatch().getDuration())
                .firstBloodTime(response.getMatch().getFirstBloodTime())
                .averageSkill(response.getMatch().getAverageSkill())
                .barracksStatusCount(response.getMatch().getBarracksStatusCount())
                .gameMode(response.getMatch().getGameMode().name())
                .build();
    }

    @Getter
    @Builder
    public static class DotaMatch {
        private int duration;
        private int firstBloodTime;
        private int averageSkill;
        private int barracksStatusCount;
        private String gameMode;
    }
}
