package com.avenga.steamclient.model;

import com.avenga.steamclient.protobufs.dota.DotaGCMessagesCommon.CMsgDOTAProfileCard;
import lombok.Getter;

@Getter
public class AccountProfileCard {

    private int accountId;
    private int badgesPoints;
    private int leaderBoardRank;
    private int leaderBoardRankCore;
    private int leaderBoardRankSupport;
    private int rankTier;
    private int previousRankTier;

    public AccountProfileCard(CMsgDOTAProfileCard profileCard) {
        this.accountId = profileCard.getAccountId();
        this.badgesPoints = profileCard.getBadgePoints();
        this.leaderBoardRank = profileCard.getLeaderboardRank();
        this.leaderBoardRankCore = profileCard.getLeaderboardRankCore();
        this.leaderBoardRankSupport = profileCard.getLeaderboardRankSupport();
        this.rankTier = profileCard.getRankTier();
        this.previousRankTier = profileCard.getPreviousRankTier();
    }
}
