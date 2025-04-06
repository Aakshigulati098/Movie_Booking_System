package com.example.movie_booking_system.dto;

import java.time.LocalDateTime;

public class BidResponseDTO {
    private String id;
    private String auctionId;
    private String bidder;
    private Long amount;
    private LocalDateTime timestamp;

    // Getters and Setters
    public BidResponseDTO() {}
    public BidResponseDTO(String id, String auctionId, String bidder, Long amount, LocalDateTime timestamp) {
        this.id = id;
        this.auctionId = auctionId;
        this.bidder = bidder;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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