package com.example.movie_booking_system.dto;

import java.io.Serializable;
import java.util.Set;

public class AuctionResultDTO implements Serializable {

    private Long auctionId;
//    remove transient keyword if found error
    private transient   BidDTO  winningBid;
    private transient Set<BidResponseDTO> leaderboard;
    private boolean noBids = false;

    // Getters and setters
    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public BidDTO getWinningBid() {
        return winningBid;
    }

    public void setWinningBid(BidDTO winningBid) {
        this.winningBid = winningBid;
    }

    public Set<BidResponseDTO> getLeaderboard() {
        return leaderboard;
    }

    public void setLeaderboard(Set<BidResponseDTO> leaderboard) {
        this.leaderboard = leaderboard;
    }

    public boolean isNoBids() {
        return noBids;
    }

    public void setNoBids(boolean noBids) {
        this.noBids = noBids;
    }

    @Override
    public String toString() {
        return "AuctionResultDTO{" +
                "auctionId=" + auctionId +
                ", winningBid=" + winningBid +
                ", leaderboard size=" + (leaderboard != null ? leaderboard.size() : 0) +
                ", noBids=" + noBids +
                '}';
    }
}
