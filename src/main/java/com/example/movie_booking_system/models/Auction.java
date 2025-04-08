package com.example.movie_booking_system.models;


import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="bookingId", referencedColumnName = "id")
    private Booking bookingId;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name="sellerId", referencedColumnName = "id")
    private Users Seller;

    @ManyToOne
    @JoinColumn(name="winnerId",referencedColumnName = "id")
    private Users Winner;

    private Long min_Amount;

    private AuctionStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime endsAt;


    public Auction() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBookingId() {
        return bookingId;
    }

    public void setBookingId(Booking bookingId) {
        this.bookingId = bookingId;
    }

    public Users getSeller() {
        return Seller;
    }

    public void setSeller(Users seller) {
        Seller = seller;
    }

    public Users getWinner() {
        return Winner;
    }

    public void setWinner(Users winner) {
        Winner = winner;
    }

    public Long getMin_Amount() {
        return min_Amount;
    }

    public void setMin_Amount(Long min_Amount) {
        this.min_Amount = min_Amount;
    }

    public AuctionStatus getStatus() {
        return status;
    }

    public void setStatus(AuctionStatus status) {
        this.status = status;
    }

    public LocalDateTime getEndsAt() {
        return endsAt;
    }

    public void setEndsAt(LocalDateTime endsAt) {
        this.endsAt = endsAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }


    public Auction(Long id, Booking bookingId, Users seller, Users winner, Long min_Amount, AuctionStatus status, LocalDateTime endsAt, LocalDateTime createdAt) {
        this.id = id;
        this.bookingId = bookingId;
        Seller = seller;
        Winner = winner;
        this.min_Amount = min_Amount;
        this.status = status;
        this.endsAt = endsAt;
        this.createdAt = createdAt;
    }
}
