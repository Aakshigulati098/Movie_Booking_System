package com.example.movie_booking_system.controller;



import com.example.movie_booking_system.dto.*;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.Users;

import com.example.movie_booking_system.repository.UserRepository;
import com.example.movie_booking_system.service.AuctionService;
import com.example.movie_booking_system.service.BidHandlerService;
import com.example.movie_booking_system.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;
import java.util.logging.Logger;


@RestController
@RequestMapping("/auction")
public class AuctionController {

    @Autowired
    private AuctionService auctionService;

    @Autowired
    private BidHandlerService bidHandlerService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private UserRepository userRepository;





    private static final java.util.logging.Logger logger = Logger.getLogger(AuctionController.class.getName());



    @GetMapping("/helloo")
    public String hello(){
        return "hello i am from auction ";
    }

    @GetMapping("/active")
    public ResponseEntity<Map<Long, Map<String, String>>> getAllActiveAuctions() {
        Map<Long, Map<String, String>> activeAuctions = redisService.getAllActiveAuctions();
        return ResponseEntity.ok(activeAuctions);
    }

    @PostMapping("/createAuction")
    public ResponseEntity<?> createAuction(@RequestBody CreateAuctionDTO incomingAuction){


        try{

            logger.info("hey i got called in auction ");
            logger.info(incomingAuction.getUserId().toString());



            Long auctionId = auctionService.createAuction(incomingAuction);
            return ResponseEntity.ok("Auction created successfully with ID: " + auctionId);

        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Auction creation failed "+e.getMessage());
        }

    }

    @PostMapping("/handleBid/{auctionId}")
    public ResponseEntity<?> handleBid(@PathVariable Long auctionId, @RequestBody BidDTO bid) {
        //this will be the function that will handle the bid
        try{
           if(bidHandlerService.handleBid(auctionId, bid)) {
                return ResponseEntity.ok("Bid placed successfully");
              } else {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Bid placement failed");
           }
        }catch(Exception e){
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Bid handling failed "+e.getMessage());
        }

    }

    @GetMapping("/leaderboard/{auctionId}")
    public ResponseEntity<Set<BidResponseDTO>> getLeaderboard(@PathVariable Long auctionId) {
        Set<BidResponseDTO> leaderboard = redisService.getLeaderboard(auctionId);
        return ResponseEntity.ok(leaderboard);
    }

    @GetMapping("/activeAuctions")
    public ResponseEntity<List<AuctionResponseDTO>> getActiveAuctions() {
        Map<Long, Map<String, String>> activeAuctions = redisService.getAllActiveAuctions();
        System.out.println("active auctions: " + activeAuctions);
        List<AuctionResponseDTO> responseList = activeAuctions.entrySet().stream().map(entry -> {
            Long auctionId = entry.getKey();
            Map<String, String> auctionData = entry.getValue();

            System.out.println("auction data: " + auctionData);

            // Fetch auction details from the database
            Auction auction = auctionService.getAuctionById(auctionId);

            if (auction == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found");
            }

            System.out.println("auction: " + auction);

            // Fetch bids for the auction
            Set<BidResponseDTO> bids = redisService.getLeaderboard(auctionId);
            System.out.println("bids here is : " + bids);

            // Create ResponseDTO
            AuctionResponseDTO response = new AuctionResponseDTO();
            System.out.println("response here is : " + response);

//        Booking booking = bookingRepository.findById(auction.getBookingId());

//bhai movie ke liye to entity mai changes karne hi padenge and we have got no other option
//        response.setMovieTitle(auction.getBookingId().getMovie().getTitle());
            response.setId(auction.getId());
            response.setTheater(auction.getBookingId().getShowtime().getTheatre().getName());
            response.setShowtime(auction.getBookingId().getShowtime().getTime());
            response.setSeat(auction.getBookingId().getSeatIds());
            response.setSellerName(auction.getSeller().getName());
            response.setBasePrice(auction.getMin_Amount());
//        this i need to figure out

            BidDTO topBid = redisService.getTopBid(auctionId);
            System.out.println("top bid here is : " + topBid);

            if (topBid != null) {
                response.setCurrentBid(topBid.getAmount());
                Users user = userRepository.findById(topBid.getUserId()).orElse(null);
                if (user != null) {
                    response.setHighestBidder(user.getName());
                } else {
                    response.setHighestBidder("Unknown");
                }
            } else {
                response.setCurrentBid(auction.getMin_Amount());
                response.setHighestBidder("No bids yet");
            }

            response.setEndTime(auction.getEndsAt());
//we do not have the movie data in the booking so need to inject that to get this data will look into it later won't be much of a issue
//        response.setImageUrl(auction.getBookingId().getMovie().getImageUrl());
//        response.setDescription(auction.getBookingId().getMovie().getDescription());
            response.setBids(new ArrayList<>(bids));
            response.setImageUrl(auction.getBookingId().getMovie().getImage());
            response.setMovieTitle(auction.getBookingId().getMovie().getTitle());

            return response;
        }).toList();
        logger.info("response list: " + responseList);

        return ResponseEntity.ok(responseList);
    }

@GetMapping("/auctionDetails/{auctionId}")
public ResponseEntity<AuctionResponseDTO> getAuctionDetails(@PathVariable Long auctionId) {
    // Fetch auction details from the database
    Auction auction = auctionService.getAuctionById(auctionId);

    if (auction == null) {
        throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found");
    }

    // Fetch bids for the auction
    Set<BidResponseDTO> bids = redisService.getLeaderboard(auctionId);

    // Create ResponseDTO
    AuctionResponseDTO response = new AuctionResponseDTO();
    response.setId(auction.getId());
    response.setTheater(auction.getBookingId().getShowtime().getTheatre().getName());
    response.setShowtime(auction.getBookingId().getShowtime().getTime());
    response.setSeat(auction.getBookingId().getSeatIds());
    response.setSellerName(auction.getSeller().getName());
    response.setBasePrice(auction.getMin_Amount());

    BidDTO topBid = redisService.getTopBid(auctionId);

    if (topBid != null) {
        response.setCurrentBid(topBid.getAmount());
        Users user = userRepository.findById(topBid.getUserId()).orElse(null);
        if (user != null) {
            response.setHighestBidder(user.getName());
        } else {
            response.setHighestBidder("Unknown");
        }
    } else {
        response.setCurrentBid(auction.getMin_Amount());
        response.setHighestBidder("No bids yet");
    }

    response.setEndTime(auction.getEndsAt());
    response.setBids(new ArrayList<>(bids));
    response.setImageUrl(auction.getBookingId().getMovie().getImage());
    response.setMovieTitle(auction.getBookingId().getMovie().getTitle());

    return ResponseEntity.ok(response);
}
    @GetMapping("/pending-payment/{userId}")
    public ResponseEntity<?> getPendingPayments(@PathVariable Long userId) {
        try {
            List<PendingAuctionDTO> pendingPayments = auctionService.getPendingPayments(userId);
            // Always return 200 OK with the list (even if empty)
            return ResponseEntity.ok(pendingPayments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error fetching pending payments: " + e.getMessage());
        }
    }
    @PutMapping("/AuctionWinRejectResponse/{userId}/{auctionId}")
    public ResponseEntity<?> handleAuctionWinRejectResponse(@PathVariable Long userId, @PathVariable Long auctionId) {
        try {
            auctionService.handleRejection(auctionId);
            return ResponseEntity.ok("Response recorded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling auction win response: " + e.getMessage());
        }
    }

//    handle action win Accept Response
//    Ab bande ne accept kar liya toh usko mast se payment ke liye bolo
//    and jab payment ho jayega auction status ko update karo and then
//    booking transfer karo and ak aur websocket connection broadcast karo
//    which will be handled by the booking service and the booking page in frontend
//    whose sole purpose is to trigger the fetching of booking details again once they get any message to this channel

    @PutMapping("/AuctionWinAcceptResponse/{userId}/{auctionId}")
    public ResponseEntity<?> handleAuctionWinAcceptResponse(@PathVariable Long userId, @PathVariable Long auctionId) {
        try {
            auctionService.handleAcceptance(auctionId,userId);
            return ResponseEntity.ok("Response recorded successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error handling auction win response: " + e.getMessage());
        }
    }

}
