package com.example.movie_booking_system.dto;

import java.time.LocalDateTime;

public class BidResponseDTO {
    private Long bidderId;
    private String auctionId;
    private String bidder;
    private Long amount;
    private LocalDateTime timestamp;

    // Getters and Setters
    public BidResponseDTO() {
//        this is a no args constructor
    }


    public Long getBidderId() {
        return bidderId;
    }

    public void setBidderId(Long bidderId) {
        this.bidderId = bidderId;
    }

    public String getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(String auctionId) {
        this.auctionId = auctionId;
    }

    public String getBidder() {
        return bidder;
    }

    public void setBidder(String bidder) {
        this.bidder = bidder;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}