package com.example.movie_booking_system.models;

import jakarta.persistence.*;

@Entity
public class AuctionWinner {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="auctionID", referencedColumnName = "id")
    private Auction auctionID;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="winnerId", referencedColumnName = "id")
    private Users winnerId;

    private Long amount;

    public AuctionWinner() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Auction getAuctionID() {
        return auctionID;
    }

    public void setAuctionID(Auction auctionID) {
        this.auctionID = auctionID;
    }

    public Users getWinnerId() {
        return winnerId;
    }

    public void setWinnerId(Users winnerId) {
        this.winnerId = winnerId;
    }

    public Long getAmount() {
        return amount;
    }

    public void setAmount(Long amount) {
        this.amount = amount;
    }

    public AuctionWinner(Long id, Auction auctionID, Users winnerId, Long amount) {
        this.id = id;
        this.auctionID = auctionID;
        this.winnerId = winnerId;
        this.amount = amount;
    }
}
