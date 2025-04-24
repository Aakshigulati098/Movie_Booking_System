package com.example.movie_booking_system.models;

import jakarta.persistence.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
public class Auction {

    @Id
    @GeneratedValue(strategy= GenerationType.SEQUENCE)
    private Long id;

    @OneToOne
    @JoinColumn(name="bookingId", referencedColumnName = "id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Booking bookingId;

    private Long FinalAmount;

    public Long getFinalAmount() {
        return FinalAmount;
    }

    public void setFinalAmount(Long finalAmount) {
        FinalAmount = finalAmount;
    }

    @ManyToOne
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

    public void setMin_Amount(Long minAmount) {
        this.min_Amount = minAmount;
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

    public Auction(Long id, Booking bookingId, Users seller, Users winner, Long minAmount, AuctionStatus status, LocalDateTime endsAt, LocalDateTime createdAt, Long finalAmount) {
        this.id = id;
        this.FinalAmount = finalAmount;
        this.bookingId = bookingId;
        Seller = seller;
        Winner = winner;
        this.min_Amount = minAmount;
        this.status = status;
        this.endsAt = endsAt;
        this.createdAt = createdAt;
    }
}