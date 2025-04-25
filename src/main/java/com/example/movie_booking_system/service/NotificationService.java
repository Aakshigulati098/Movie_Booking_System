package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;


import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


import java.util.Optional;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());


    private AuctionAsyncService auctionAsyncService;
    private AuctionWinnerRepository auctionWinnerRepository;
    private AuctionRepository auctionRepository;
    private UserRepository userRepository;

    @Autowired
    public NotificationService(AuctionAsyncService auctionAsyncService,
                                AuctionWinnerRepository auctionWinnerRepository,
                                AuctionRepository auctionRepository,
                                UserRepository userRepository) {
        this.auctionAsyncService = auctionAsyncService;
        this.auctionWinnerRepository = auctionWinnerRepository;
        this.auctionRepository = auctionRepository;
        this.userRepository = userRepository;
    }


////        fact is here i need to find out a way to persist the data or i should be handling it in the consumer itself
////        this file should not know so much of information as its task is just to notify !!!!

    public void sendNotification(BidDTO bidder) {

        logger.info("idhar ho rha hai ");
        logger.info("Sending notification from the notification service to " + bidder.getUserId() + ": " + bidder.getAmount());
//        agar mujhe notification bhejna hai toh mujhe ye cheezein karni padengi
//        1. Email
//        2. Db mai ak table banao ke iss auction id pe current payer yeh hai and then send a websocket notification
//        ak simple sa model hoga with auctionId,winnerId thats it and bss usi ko change karta rahunga

//        first check karo bhai asa koi auction toh nahi hai na idhar

        Auction auction = auctionRepository.findById(bidder.getAuctionId()).orElse(null);
        if(auction == null) {
            logger.info("No auction found for " + bidder.getAuctionId());
            return;
        }
        Optional<AuctionWinner> existingAuction=auctionWinnerRepository.findByAuctionID(auction);
        if(existingAuction.isEmpty()) {
            logger.info("No auction winner found for " + bidder.getAuctionId());

        }


        Users winner=userRepository.findById(bidder.getUserId()).orElse(null);
        if(winner == null) {
            logger.info("No winner found for " + bidder.getUserId());
            return;
        }

        auctionAsyncService.saveWinnerAndBroadcast(bidder, auction, winner,
                existingAuction.orElse(null));
        logger.info("Auction updated for " + bidder.getUserId() +" and websocket notification sent");
//        now need to handle accept and reject thing which is not our headache this will be solved by a diff api





//        ab frontend pe home page pe and pending payment page pe ye cheezein update ho jayengi by websocket connection
//        and also keep a case of when auction ends the auction screen should redirect the user to the home screen or
//        the main auctions page !

        // For now, just log the notification
        // You can replace this with your actual notification mechanism
        // such as email, push notifications, etc.
    }

//    @SendTo("/")
}