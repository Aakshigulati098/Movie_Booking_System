package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

@Service
public class NotificationService {

    private static final Logger logger = Logger.getLogger(NotificationService.class.getName());

    @Autowired
    private AuctionAsyncService auctionAsyncService;


    @Autowired
    private AuctionWinnerRepository auctionWinnerRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

//    public void notifyAuctionParticipants(AuctionResultDTO result) {
//        logger.info("Sending notifications for auction: " + result.getAuctionId());
//
//        // If there were no bids
//        if (result.isNoBids()) {
//            logger.info("No bids for auction " + result.getAuctionId() + ", no notifications to send");
//            return;
//        }
//
////        fact is here i need to find out a way to persist the data or i should be handling it in the consumer itself
////        this file should not know so much of information as its task is just to notify !!!!
//
//        // Get the set of all participants
//        Set<Long> participantIds = extractParticipantIds(result);
//
//        // Notify the winner
//        if (result.getWinningBid() != null) {
//            Long winnerId = result.getWinningBid().getUserId();
//            Optional<Users> winner = userRepository.findById(winnerId);
//
//            winner.ifPresent(user -> {
//                String message = "Congratulations! You've won the auction #" +
//                        result.getAuctionId() +
//                        " with a bid of $" + result.getWinningBid().getAmount();
//
//                sendNotification(user, "Auction Won!", message);
//                participantIds.remove(winnerId); // Remove winner from participants list
//            });
//        }
//
//        // Notify other participants
//        for (Long participantId : participantIds) {
//            Optional<Users> participant = userRepository.findById(participantId);
//
//            participant.ifPresent(user -> {
//                String message = "Auction #" + result.getAuctionId() +
//                        " has ended. Thank you for participating!";
//
//                sendNotification(user, "Auction Ended", message);
//            });
//        }
//    }

//    private Set<Long> extractParticipantIds(AuctionResultDTO result) {
//        Set<Long> participantIds = new HashSet<>();
//
//
//        // Add winner if exists
//        if (result.getWinningBid() != null) {
//            participantIds.add(result.getWinningBid().getUserId());
//        }
//
//        // Add all bidders from leaderboard
//        if (result.getLeaderboard() != null) {
//            // Since we don't have direct access to user IDs in the leaderboard,
//            // We'll need to implement a proper extraction mechanism
//            // This is just a placeholder for now
//            logger.info("Extracted " + participantIds.size() + " participant IDs");
//        }
//
//        return participantIds;
//    }



//        this is a part of the notification service that i have and i have to modify
//        notify kaise karoge is a thing that ak table create karo and then websocket ke madat se update kardo
//        and usi websocket ke madat se home page pe toaster aa jayega
//        and usi websocket ke madat se pending payment page update ho jayega which will take the user Id and check
//        ke bhai kisi bhi AUCTION  mai iska pending payment to nahi hai and then usi hisab se mai ak api banakar sari cheezo ko show karwa dunga

//        ab bande ne reject kar diya toh ak api hit karoge jo ki same produce function ko call karega and this will repeat
//        handle the edge case jab koi bidder hi na bache uss case mai auction ke status ko unsold kar dena hai
//        yad rakhna auctionStatus ke enum mai humko bohot kam karna hai then

//        and agar accept kar liya then we will be focussing on the db cleanup , db updation , payment service and ticket transfer

//    the fact that here i will just be receiving a bidDTO object
    public void sendNotification(BidDTO bidder) {
        // Implementation depends on your notification mechanism
//        logger.info("Sending notification to " + user.getName() + ": " + subject);


//        again humko rejection ke do part hone wale hai
//        yes or no via email
//        via again a redis expiry for the bidding purpose


//        pehle db wala implement karo because that will apply some backend part
//        as email wale mai bss ak link inject karke bhejna hai thats it
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
//            return;
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