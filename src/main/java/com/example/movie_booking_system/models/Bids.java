package com.example.movie_booking_system.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Bids {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "bidderId", referencedColumnName = "id")
    private Users userId;

    @ManyToOne
    @JoinColumn(name="auctionId",referencedColumnName = "id")
    private Auction auctionId;

    private Long BidAmount;

    private LocalDateTime createdAt;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Users getUserId() {
        return userId;
    }

    public void setUserId(Users userId) {
        this.userId = userId;
    }

    public Auction getAuctionId() {
        return auctionId;
    }

    public void setAuctionId(Auction auctionId) {
        this.auctionId = auctionId;
    }

    public Long getBidAmount() {
        return BidAmount;
    }

    public void setBidAmount(Long bidAmount) {
        BidAmount = bidAmount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Bids(){};

    public Bids(Long id, Users userId, Auction auctionId, Long bidAmount, LocalDateTime createdAt) {
        this.id = id;
        this.userId = userId;
        this.auctionId = auctionId;
        BidAmount = bidAmount;
        this.createdAt = createdAt;
    }
}
