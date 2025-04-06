package com.example.movie_booking_system.dto;
import java.time.LocalDateTime;
import java.util.List;

public class AuctionResponseDTO {
    private Long id;
    private String movieTitle;
    private String theater;
    private String showtime;
    private String seat;
    private String sellerName;
    private Long basePrice;
    private Long currentBid;
    private String highestBidder;
    private LocalDateTime endTime;
    private String imageUrl;
    private String description;
    private List<BidResponseDTO> bids;

    // Getters and Setters


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMovieTitle() {
        return movieTitle;
    }

    public void setMovieTitle(String movieTitle) {
        this.movieTitle = movieTitle;
    }

    public String getTheater() {
        return theater;
    }

    public void setTheater(String theater) {
        this.theater = theater;
    }

    public String getShowtime() {
        return showtime;
    }

    public void setShowtime(String showtime) {
        this.showtime = showtime;
    }

    public String getSeat() {
        return seat;
    }

    public void setSeat(String seat) {
        this.seat = seat;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public Long getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(Long basePrice) {
        this.basePrice = basePrice;
    }

    public Long getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(Long currentBid) {
        this.currentBid = currentBid;
    }

    public String getHighestBidder() {
        return highestBidder;
    }

    public void setHighestBidder(String highestBidder) {
        this.highestBidder = highestBidder;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<BidResponseDTO> getBids() {
        return bids;
    }

    public void setBids(List<BidResponseDTO> bids) {
        this.bids = bids;
    }

    public AuctionResponseDTO() {
    }

    public AuctionResponseDTO(Long id, String movieTitle, String theater, String showtime, String seat, String sellerName, Long basePrice, Long currentBid, String highestBidder, LocalDateTime endTime, String imageUrl, String description, List<BidResponseDTO> bids) {
        this.id = id;
        this.movieTitle = movieTitle;
        this.theater = theater;
        this.showtime = showtime;
        this.seat = seat;
        this.sellerName = sellerName;
        this.basePrice = basePrice;
        this.currentBid = currentBid;
        this.highestBidder = highestBidder;
        this.endTime = endTime;
        this.imageUrl = imageUrl;
        this.description = description;
        this.bids = bids;
    }
}

