package com.example.movie_booking_system.controller;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WebSocketControllerTest {

    private final WebSocketController webSocketController = new WebSocketController();

    @Test
    void broadcastBid_ShouldReturnUpdatedLeaderboardMessage() {
        Long auctionId = 123L;
        String expectedMessage = "Leaderboard for auction " + auctionId + " has been updated.";

        String result = webSocketController.broadcastBid(auctionId);

        assertEquals(expectedMessage, result);
    }

    @Test
    void updateAuction_ShouldReturnPostAuctionUpdateMessage() {
        String expectedMessage = "User has been notified for post auction updates !!!";

        String result = webSocketController.updateAuction();

        assertEquals(expectedMessage, result);
    }

    @Test
    void updateBooking_ShouldReturnBookingModifiedMessage() {
        String expectedMessage = "Booking has been modified due to a auction !!!";

        String result = webSocketController.updateBooking();

        assertEquals(expectedMessage, result);
    }

    @Test
    void updateAuctionAcceptance_ShouldReturnAuctionAcceptedMessage() {
        String expectedMessage = "Auction has been accepted !!!";

        String result = webSocketController.updateAuctionAcceptance();

        assertEquals(expectedMessage, result);
    }
}