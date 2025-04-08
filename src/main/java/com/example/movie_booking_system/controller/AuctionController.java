package com.example.movie_booking_system.controller;


import com.example.movie_booking_system.dto.AuctionResponseDTO;
import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.dto.createAuctionDTO;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.BookingRepository;
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
import java.util.stream.Collectors;

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


    @Autowired
    private BookingRepository bookingRepository;

    @GetMapping("/helloo")
    public String hello(){
        return "hello i am from auction ";
    }

//    here i need to define redis also i think because at a point i need to store the auction data in redis

    @GetMapping("/active")
    public ResponseEntity<Map<Long, Map<String, String>>> getAllActiveAuctions() {
        Map<Long, Map<String, String>> activeAuctions = redisService.getAllActiveAuctions();
        return ResponseEntity.ok(activeAuctions);
    }

    @PostMapping("/createAuction")
    public ResponseEntity<?> createAuction(@RequestBody createAuctionDTO Incomingauction){

//        i should be creating a auction service function
//            which will internally call a function to check for validity
//        and if it is valid it will be creating the new entry in the Auction table
//            for that i guess we will be in need of a few things
//                bookingId,userId,minimumAmount,
        try{

            System.out.println("hey i got called in auction ");
            System.out.println(Incomingauction);
//            return ResponseEntity.ok("Auction created successfully with ID: " + Incomingauction.getBookingId());


            Long auctionId = auctionService.createAuction(Incomingauction);
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
            response.setId(auction.getId());
//        Booking booking = bookingRepository.findById(auction.getBookingId());

//bhai movie ke liye to entity mai changes karne hi padenge and we have got no other option
//        response.setMovieTitle(auction.getBookingId().getMovie().getTitle());
            response.setTheater(auction.getBookingId().getShowtime().getTheatre().getName());

            response.setShowtime(auction.getBookingId().getShowtime().toString());
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

            return response;
        }).collect(Collectors.toList());
        System.out.println("response list: " + responseList);

        return ResponseEntity.ok(responseList);
    }
//    one api point would be to validate and create the auction data
            //this would be to validate the auction creation eligibility
//          //if not eligible return error
            //if eligible call a service that will create an entry and return the auction id
//          //and then return the auction id to the user
//    another would be to handle the bids
            //this function sole work is to validate the bid
            //if the bid is valid then it will call a service that will update the auction data both in redis and the database
            //and then broadcast that there is  a change  to all the users using websockets
//    another would be to get the auction data from redis
        //this sole use case is to get the auction id and return the leaderboard for that auction id

//    another would be to handle the auction end and notify the winner
    //    this would include the use case of kafka consumer so will look into it later
//    and also i guess the validation of timer should be done both in the frontend and the backend seperately

//    for the frontend i guess we can use react and chatgpt can help us with that
//    for te backend redis has ttl and after it ends we should find out a function and then when it ends
//    it should automatically call this function which handles the auction end with kafka consumer
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
    response.setShowtime(auction.getBookingId().getShowtime().toString());
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

    return ResponseEntity.ok(response);
}
}
