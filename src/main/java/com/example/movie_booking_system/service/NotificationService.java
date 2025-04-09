package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;

import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private UserRepository userRepository;

    public void notifyAuctionParticipants(AuctionResultDTO result) {
        logger.info("Sending notifications for auction: " + result.getAuctionId());

        // If there were no bids
        if (result.isNoBids()) {
            logger.info("No bids for auction " + result.getAuctionId() + ", no notifications to send");
            return;
        }

        // Get the set of all participants
        Set<Long> participantIds = extractParticipantIds(result);

        // Notify the winner
        if (result.getWinningBid() != null) {
            Long winnerId = result.getWinningBid().getUserId();
            Optional<Users> winner = userRepository.findById(winnerId);

            winner.ifPresent(user -> {
                String message = "Congratulations! You've won the auction #" +
                        result.getAuctionId() +
                        " with a bid of $" + result.getWinningBid().getAmount();

                sendNotification(user, "Auction Won!", message);
                participantIds.remove(winnerId); // Remove winner from participants list
            });
        }

        // Notify other participants
        for (Long participantId : participantIds) {
            Optional<Users> participant = userRepository.findById(participantId);

            participant.ifPresent(user -> {
                String message = "Auction #" + result.getAuctionId() +
                        " has ended. Thank you for participating!";

                sendNotification(user, "Auction Ended", message);
            });
        }
    }

    private Set<Long> extractParticipantIds(AuctionResultDTO result) {
        Set<Long> participantIds = new HashSet<>();

        // Add winner if exists
        if (result.getWinningBid() != null) {
            participantIds.add(result.getWinningBid().getUserId());
        }

        // Add all bidders from leaderboard
        if (result.getLeaderboard() != null) {
            // Since we don't have direct access to user IDs in the leaderboard,
            // We'll need to implement a proper extraction mechanism
            // This is just a placeholder for now
            logger.info("Extracted " + participantIds.size() + " participant IDs");
        }

        return participantIds;
    }

    private void sendNotification(Users user, String subject, String message) {
        // Implementation depends on your notification mechanism
        logger.info("Sending notification to " + user.getName() + ": " + subject);

        // For now, just log the notification
        // You can replace this with your actual notification mechanism
        // such as email, push notifications, etc.
    }
}