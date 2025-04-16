package com.example.movie_booking_system.dto;


public class BidDTO {
    private Long auctionId;
    private Long userId;
    private Long amount;

    public BidDTO() {}


    public BidDTO(Long auctionId, Long userId, Long amount) {
        this.auctionId = auctionId;
        this.userId = userId;
        this.amount = amount;
    }

    public Long getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Long auctionId) {
        this.auctionId = auctionId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

}
