package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class AuctionAsyncService {

    private static final Logger logger = Logger.getLogger(AuctionAsyncService.class.getName());

    @Autowired
    private AuctionWinnerRepository auctionWinnerRepository;

    @Autowired
    private WebSocketService webSocketService; // WebSocket broadcaster

    @Autowired
    private AuctionRepository auctionRepository;

    @Async
    @Transactional
    public void saveWinnerAndBroadcast(BidDTO bidder, Auction auction, Users winner, AuctionWinner existingAuction) {
        try {

            // Re-fetch the auction to make it managed in this transaction context
            Auction managedAuction = auctionRepository.findById(bidder.getAuctionId()).orElse(null);

            if (managedAuction == null) {
                logger.warning("Auction not found in async context for ID " + bidder.getAuctionId());
                return;
            }
            Users managedWinner = managedAuction.getWinner();
            logger.info("Managed winner fetched for auction ID " + managedWinner.getName());
            if (existingAuction == null) {
                AuctionWinner auctionWinner = new AuctionWinner();
                auctionWinner.setAuctionID(managedAuction);
                auctionWinner.setWinnerId(managedWinner);
                auctionWinner.setAmount(bidder.getAmount());

                auctionWinnerRepository.save(auctionWinner);
                logger.info("Auction Winner saved for auction ID " + bidder.getAuctionId());
            } else {
                existingAuction.setWinnerId(winner);
                existingAuction.setAmount(bidder.getAmount());

                auctionWinnerRepository.save(existingAuction);
                logger.info("Auction Winner updated for auction ID " + bidder.getAuctionId());
            }

            // After successful save, broadcast WebSocket update
            webSocketService.sendWinnerNotification(bidder);
            logger.info("Winner notification sent for auction ID " + bidder.getAuctionId());

        } catch (Exception e) {
            logger.severe("Error in async auction save & notify: " + e.getMessage());
        }
    }
}
