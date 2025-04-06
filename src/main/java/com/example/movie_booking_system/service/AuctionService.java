package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.createAuctionDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionStatus;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.BidsRepository;
import com.example.movie_booking_system.repository.BookingRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Map;

@Service
public class AuctionService {

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BidsRepository bidsRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private RedisService redisService;

    public Long createAuction(createAuctionDTO Incomingauction) throws ResponseStatusException {
        // Check if the bookingId is valid to be created as an auction
        boolean isValid = true; // Here I will be calling a function with the bookingId

        if (isValid) {
            // Create a new auction
            Auction auction = new Auction();
            auction.setStatus(AuctionStatus.ACTIVE);
            auction.setCreatedAt(LocalDateTime.now());
            auction.setMin_Amount(Incomingauction.getMinAmount());
            auction.setSeller(userRepository.findById(Incomingauction.getUserId()).orElse(null));
            // Set the end time to 1 hour from now
            auction.setEndsAt(LocalDateTime.now().plusHours(1));
            // Set the booking ID
            auction.setBookingId(bookingRepository.findById(Incomingauction.getBookingId()).orElse(null));

            // Save the auction to the database
            Auction savedAuction = auctionRepository.save(auction);

            // Store auction metadata in Redis
            redisService.saveAuctionMetadata(savedAuction.getId(), "ACTIVE", savedAuction.getEndsAt());
            redisService.createLeaderboard(savedAuction.getId());

            return savedAuction.getId(); // Return the ID of the created auction
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid booking ID for auction creation");
        }
    }

    public Map<Long, Map<String, String>> getAllActiveAuctions() {
        return redisService.getAllActiveAuctions();
    }

    public Auction getAuctionById(Long id) {
        return auctionRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found"));
    }
}