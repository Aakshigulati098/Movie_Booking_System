package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.models.Bids;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.BidsRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

@Service
public class BidHandlerService {

    private static final Logger logger = Logger.getLogger(BidHandlerService.class.getName());

    @Autowired
    private BidsRepository bidsRepository;

    @Autowired
    private RedisService redisService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private static final String LOCK_KEY_PREFIX = "lock:auction:";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    @Transactional
    public boolean handleBid(Long auctionId, BidDTO bid) {
        String lockKey = LOCK_KEY_PREFIX + auctionId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);

        if (Boolean.TRUE.equals(acquired)) {
            try {
                logger.info("Lock acquired for auction ID: " + auctionId);

                // Validate the bid
                Optional<BidDTO> topBidOpt = Optional.ofNullable(redisService.getTopBid(auctionId));

                // Check if the user is the auction owner
                Long auctionIdd=bid.getAuctionId();
                Long auctionOwnerId=auctionRepository.findById(auctionIdd)
                        .orElseThrow(() -> new IllegalArgumentException("Auction not found"))
                        .getSeller()
                        .getId();

                if (bid.getUserId().equals(auctionOwnerId)) {
                    throw new IllegalArgumentException("You cannot bid on your own auction.");
                }

                // If there is a top bid, validate the new bid is higher
                topBidOpt.ifPresent(topBid -> {
                    if (bid.getAmount() <= topBid.getAmount()) {
                        throw new IllegalArgumentException("Bid amount must be higher than the current highest bid.");
                    }
                });

                // Check if the auction is active
                String auctionStatus = (String) redisTemplate.opsForHash().get("auction" + auctionId, "status");
                logger.info("Auction status: " + auctionStatus);
                if (!"ACTIVE".equals(auctionStatus)) {
                    throw new IllegalArgumentException("Auction is not active.");
                }

                // Check if the auction has ended
                String endTimeStr = (String) redisTemplate.opsForHash().get("auction" + auctionId, "endTime");
                if (endTimeStr != null) {
                    LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
                    if (LocalDateTime.now().isAfter(endTime)) {
                        throw new IllegalArgumentException("Auction has already ended.");
                    }
                }

                // Check if the user already has a bid for this auction
                Optional<Bids> existingBid = bidsRepository.findByUserIdIdAndAuctionIdId(bid.getUserId(), bid.getAuctionId());

                Bids bids;
                if (existingBid.isPresent()) {
                    // Update existing bid
                    bids = existingBid.get();
                    bids.setBidAmount(bid.getAmount());
                    bids.setCreatedAt(LocalDateTime.now()); // Update timestamp to current time
                    logger.info("Updating existing bid for user " + bid.getUserId() + " on auction " + bid.getAuctionId());
                } else {
                    // Create new bid
                    bids = new Bids();
                    bids.setBidAmount(bid.getAmount());
                    bids.setUserId(userRepository.findById(bid.getUserId()).orElseThrow(() -> new IllegalArgumentException("User not found")));
                    bids.setAuctionId(auctionRepository.findById(bid.getAuctionId()).orElseThrow(() -> new IllegalArgumentException("Auction not found")));
                    bids.setCreatedAt(LocalDateTime.now());
                    logger.info("Creating new bid for user " + bid.getUserId() + " on auction " + bid.getAuctionId());
                }

                // Update the Redis leaderboard
                redisService.addBidToLeaderboard(auctionId, bid);
                logger.info("Redis leaderboard updated");

                // Broadcast the change via WebSocket
                messagingTemplate.convertAndSend("/topic/auction/" + auctionId, "Leaderboard for auction " + auctionId + " has been updated.");
                logger.info("WebSocket message sent");

                bidsRepository.save(bids);
                logger.info("Bid saved to database");

                return true;

            } catch (IllegalArgumentException e) {
                logger.warning("Validation error: " + e.getMessage());
                return false;
            } catch (Exception e) {
                logger.severe("Error handling bid: " + e.getMessage());
                return false;
            } finally {
                redisTemplate.delete(lockKey);
                logger.info("Lock released for auction ID: " + auctionId);
            }
        } else {
            logger.warning("Bid handling is already in progress for auction ID: " + auctionId);
            return false;
        }
    }
}