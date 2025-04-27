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


    private BidsRepository bidsRepository;
    private RedisService redisService;
    private RedisTemplate<String, Object> redisTemplate;
    private SimpMessagingTemplate messagingTemplate;
    private UserRepository userRepository;
    private AuctionRepository auctionRepository;
    @Autowired
    public BidHandlerService(BidsRepository bidsRepository, RedisService redisService,
                             RedisTemplate<String, Object> redisTemplate,
                             SimpMessagingTemplate messagingTemplate,
                             UserRepository userRepository,
                             AuctionRepository auctionRepository) {
        this.bidsRepository = bidsRepository;
        this.redisService = redisService;
        this.redisTemplate = redisTemplate;
        this.messagingTemplate = messagingTemplate;
        this.userRepository = userRepository;
        this.auctionRepository = auctionRepository;
    }
    private static final String LOCK_KEY_PREFIX = "lock:auction:";


    @Transactional
    public boolean handleBid(Long auctionId, BidDTO bid) {
        String lockKey = LOCK_KEY_PREFIX + auctionId;
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked", 10, TimeUnit.SECONDS);

        if (!Boolean.TRUE.equals(acquired)) {
            logger.warning("Bid handling is already in progress for auction ID: " + auctionId);
            return false;
        }

        try {
            logger.info("Lock acquired for auction ID: " + auctionId);
            validateBidAndAuction(auctionId, bid);
            return processBid(auctionId, bid);
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
    }

    private void validateBidAndAuction(Long auctionId, BidDTO bid) {
        validateAuctionOwner(bid);
        validateTopBid(auctionId, bid);
        validateAuctionStatus(auctionId);
        validateAuctionEndTime(auctionId);
    }

    private void validateAuctionOwner(BidDTO bid) {
        Long auctionOwnerId = auctionRepository.findById(bid.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found"))
                .getSeller()
                .getId();

        if (bid.getUserId().equals(auctionOwnerId)) {
            throw new IllegalArgumentException("You cannot bid on your own auction.");
        }
    }

    private void validateTopBid(Long auctionId, BidDTO bid) {
        Optional.ofNullable(redisService.getTopBid(auctionId))
                .ifPresent(topBid -> {
                    if (bid.getAmount() <= topBid.getAmount()) {
                        throw new IllegalArgumentException("Bid amount must be higher than the current highest bid.");
                    }
                });
    }

    private void validateAuctionStatus(Long auctionId) {
        String auctionStatus = (String) redisTemplate.opsForHash().get("auction" + auctionId, "status");
        logger.info("Auction status: " + auctionStatus);
        if (!"ACTIVE".equals(auctionStatus)) {
            throw new IllegalArgumentException("Auction is not active.");
        }
    }

    private void validateAuctionEndTime(Long auctionId) {
        String endTimeStr = (String) redisTemplate.opsForHash().get("auction" + auctionId, "endTime");
        if (endTimeStr != null) {
            LocalDateTime endTime = LocalDateTime.parse(endTimeStr);
            if (LocalDateTime.now().isAfter(endTime)) {
                throw new IllegalArgumentException("Auction has already ended.");
            }
        }
    }

    private boolean processBid(Long auctionId, BidDTO bid) {
        Bids bids = updateOrCreateBid(bid);
        updateRedisAndNotify(auctionId, bid);
        bidsRepository.save(bids);
        logger.info("Bid saved to database");
        return true;
    }

    private Bids updateOrCreateBid(BidDTO bid) {
        Optional<Bids> existingBid = bidsRepository.findByUserIdIdAndAuctionIdId(
                bid.getUserId(), bid.getAuctionId());

        if (existingBid.isPresent()) {
            return updateExistingBid(existingBid.get(), bid);
        }
        return createNewBid(bid);
    }

    private Bids updateExistingBid(Bids existingBid, BidDTO bid) {
        existingBid.setBidAmount(bid.getAmount());
        existingBid.setCreatedAt(LocalDateTime.now());
        logger.info("Updating existing bid for user " + bid.getUserId() +
                " on auction " + bid.getAuctionId());
        return existingBid;
    }

    private Bids createNewBid(BidDTO bid) {
        Bids newBid = new Bids();
        newBid.setBidAmount(bid.getAmount());
        newBid.setUserId(userRepository.findById(bid.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found")));
        newBid.setAuctionId(auctionRepository.findById(bid.getAuctionId())
                .orElseThrow(() -> new IllegalArgumentException("Auction not found")));
        newBid.setCreatedAt(LocalDateTime.now());
        logger.info("Creating new bid for user " + bid.getUserId() +
                " on auction " + bid.getAuctionId());
        return newBid;
    }

    private void updateRedisAndNotify(Long auctionId, BidDTO bid) {
        redisService.addBidToLeaderboard(auctionId, bid);
        logger.info("Redis leaderboard updated");
        messagingTemplate.convertAndSend("/topic/auction/" + auctionId,
                "Leaderboard for auction " + auctionId + " has been updated.");
        logger.info("WebSocket message sent");
    }
}