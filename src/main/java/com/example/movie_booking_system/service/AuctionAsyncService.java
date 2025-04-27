package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.emailotp.OtpEmailController;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionSynchronization;

import java.util.Optional;
import java.util.logging.Logger;

@Service
public class AuctionAsyncService {

    private static final Logger logger = Logger.getLogger(AuctionAsyncService.class.getName());


    private AuctionWinnerRepository auctionWinnerRepository;
    private WebSocketService webSocketService;
    private UserRepository userRepository;
    private AuctionRepository auctionRepository;
    private OtpEmailController otpEmailController;

    @Autowired
    public AuctionAsyncService(AuctionWinnerRepository auctionWinnerRepository,
                                WebSocketService webSocketService,
                                UserRepository userRepository,
                                AuctionRepository auctionRepository,
                                OtpEmailController otpEmailController) {
        this.auctionWinnerRepository = auctionWinnerRepository;
        this.webSocketService = webSocketService;
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
        this.otpEmailController = otpEmailController;
    }

    @Async
    @Transactional
    public void saveWinnerAndBroadcast(BidDTO bidder, Auction auction, Users winner, AuctionWinner existingAuction) {
        try {
            logger.info("hey i am here in saveWiner");
            // Re-fetch the auction to make it managed in this transaction context
            logger.info("here the auction id is "+bidder.getAuctionId());
            Auction managedAuction = auctionRepository.findById(bidder.getAuctionId())
                    .orElseThrow(() -> new RuntimeException("Auction not found: " + bidder.getAuctionId()));
            logger.info("the auction id here is "+managedAuction.getId());


//            managed winner ko auctionWinnerRepository se lena hai
            Users managedWinner= userRepository.findById(bidder.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found: " + bidder.getUserId()));



            // Check if winner exists in database for this auction
            Optional<AuctionWinner> currentWinner = auctionWinnerRepository.findByAuctionID(managedAuction);

            if (currentWinner.isPresent()) {
                // Update existing winner
                logger.info("Updating existing auction winner for auction ID: " + bidder.getAuctionId());
                currentWinner.get().setWinnerId(managedWinner);
                currentWinner.get().setAmount(bidder.getAmount());
                auctionWinnerRepository.save(currentWinner.get());
            } else {
                // Create new winner
                logger.info("Creating new auction winner for auction ID: " + bidder.getAuctionId());
                AuctionWinner auctionWinner = new AuctionWinner();
                auctionWinner.setAuctionID(managedAuction);
                auctionWinner.setWinnerId(managedWinner);
                auctionWinner.setAmount(bidder.getAmount());
                auctionWinnerRepository.save(auctionWinner);
            }

            // Register callback for after-commit operations
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    otpEmailController.sendAuctionWinningMail(managedWinner, managedAuction);
                    webSocketService.sendWinnerNotification(bidder);
                    logger.info("Winner notification sent for auction ID " + bidder.getAuctionId());
                }
            });

        } catch (Exception e) {
            throw e;
        }

    }
}