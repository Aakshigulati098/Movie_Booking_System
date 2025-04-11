package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.DefaultTypedTuple;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class RedisService {

    private static final Logger logger = Logger.getLogger(RedisService.class.getName());

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuctionRepository auctionRepository;

    public void saveAuctionMetadata(Long auctionId, String status, LocalDateTime endTime) {
//        !here this is my key basically for storing the auction status
        String key = "auction" + auctionId;
        redisTemplate.opsForHash().put(key, "status", status);
        redisTemplate.opsForHash().put(key, "endTime", endTime.toString());
        long secondsToExpire = Math.max(0, java.time.Duration.between(LocalDateTime.now(), endTime).getSeconds());
        redisTemplate.expire(key, secondsToExpire, TimeUnit.SECONDS); // Set TTL for 1 hour
//        we are setting the ttl here so any thing related to expiry in terms of redis can be handled here
//        we would reduce this  time to 2 min so that we can test the functionality
    }

    public Map<Long, Map<String, String>> getAllActiveAuctions() {
        System.out.println("hey i am here in getAllActiveAuctions redis");
        Set<String> keys = redisTemplate.keys("auction*");
        System.out.println("Keys of all active auctions: " + keys);
        return keys.stream()
                .filter(key -> !key.contains(":bids")&& !key.contains(":user:") &&!key.contains(":leaderboard") && "ACTIVE".equals(redisTemplate.opsForHash().get(key, "status")))
                .collect(Collectors.toMap(
                        key -> Long.parseLong(key.replace("auction", "")),
                        key -> redisTemplate.<String, String>opsForHash().entries(key)
                ));
    }

    public void createLeaderboard(Long auctionId) {
        // No need to initialize the ZSET, it will be created when the first bid is added
        String key = "auction" + auctionId + ":bids";

        Auction auction=auctionRepository.findById(auctionId).orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        long secondsToExpire = Math.max(0, java.time.Duration.between(LocalDateTime.now(), auction.getEndsAt()).getSeconds());

        redisTemplate.expire(key, secondsToExpire, TimeUnit.SECONDS);
//        here i am setting the ttl rather than at a place where i will have control over it so i think need to rethink on it a bit
    }

    /**
     * Add a bid to the leaderboard, ensuring only one entry per user
     * If the user has already bid, their previous bid will be replaced
     */
    public void addBidToLeaderboard(Long auctionId, BidDTO bid) {
        String key = "auction" + auctionId + ":bids";
        String userKey = "auction:" + auctionId + ":user:" + bid.getUserId();

        // First, check if this user has a previous bid in this auction
        BidDTO existingBid = getUserBid(auctionId, bid.getUserId());

        if (existingBid != null) {
            // Remove the old bid
            logger.info("Removing previous bid for user " + bid.getUserId() + " in auction " + auctionId);
            redisTemplate.opsForZSet().remove(key, existingBid);
        }

        // Add the new bid
        redisTemplate.opsForZSet().add(key, bid, bid.getAmount());

        Auction auction=auctionRepository.findById(auctionId).orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        long secondsToExpire = Math.max(0, java.time.Duration.between(LocalDateTime.now(), auction.getEndsAt()).getSeconds());

        // Store the user's latest bid in a separate key for quick lookup
        redisTemplate.opsForValue().set(userKey, bid);
        redisTemplate.expire(userKey, secondsToExpire, TimeUnit.SECONDS);

        logger.info("Added/updated bid for user " + bid.getUserId() + " in auction " + auctionId + " with amount " + bid.getAmount());
    }

    /**
     * Get a user's current bid for an auction
     */
    public BidDTO getUserBid(Long auctionId, Long userId) {
        String userKey = "auction:" + auctionId + ":user:" + userId;
        Object userBid = redisTemplate.opsForValue().get(userKey);

        if (userBid != null) {
            return (BidDTO) userBid;
        }
//        agar userBid null hai toh fir toh user ne kabhi part liya hi nahi hai na ?

        // If not found in the direct lookup, search in the leaderboard
        String leaderboardKey = "auction" + auctionId + ":bids";
        Set<Object> bids = redisTemplate.opsForZSet().range(leaderboardKey, 0, -1);

        Auction auction=auctionRepository.findById(auctionId).orElseThrow(() -> new IllegalArgumentException("Auction not found"));

        long secondsToExpire = Math.max(0, java.time.Duration.between(LocalDateTime.now(), auction.getEndsAt()).getSeconds());

        if (bids != null) {
            for (Object bidObj : bids) {
                System.out.println("here the object bidObj is : " + bidObj);
                BidDTO bidDTO = (BidDTO) bidObj;
                System.out.println("here the bidDTO is: " + bidDTO);

//                if i am getting a bid match then what am i sending here should not i directly send ?
                if (bidDTO.getUserId().equals(userId)) {
                    // Cache this for future lookups
                    redisTemplate.opsForValue().set(userKey, bidDTO);
                    redisTemplate.expire(userKey, secondsToExpire, TimeUnit.SECONDS);
                    return bidDTO;
                }
            }
        }

//so if the user has never taken part i am absolutely sure here and that is the reason why i am sending nul here

        return null;
    }

    public BidDTO getTopBid(Long auctionId) {
        String key = "auction" + auctionId + ":bids";
        Set<ZSetOperations.TypedTuple<Object>> topBids = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 0);

        if (topBids != null && !topBids.isEmpty()) {
            ZSetOperations.TypedTuple<Object> topBid = topBids.iterator().next();
            if (topBid != null) {
                return (BidDTO) topBid.getValue();
            }
        }

        return null; // Return null if there are no bids
    }

    public Set<BidResponseDTO> getLeaderboard(Long auctionId) {
        System.out.println("hey i am here in leaderboard");
        String key = "auction" + auctionId + ":bids";
        System.out.println("Key: " + key);
        Set<ZSetOperations.TypedTuple<Object>> bids = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, -1);
        System.out.println("Fetched bids: " + bids);
        if (bids == null || bids.isEmpty()) {
            return Collections.emptySet();
        }
        return bids.stream().map(bid -> {
            BidDTO bidDTO = (BidDTO) bid.getValue();
            if (bidDTO == null) {
                throw new IllegalArgumentException("Invalid bid data for auction ID: " + auctionId);
            }

            BidResponseDTO bidResponseDTO = new BidResponseDTO();
            bidResponseDTO.setBidderId(bidDTO.getUserId());
            bidResponseDTO.setAuctionId(bidDTO.getAuctionId().toString());
            bidResponseDTO.setBidder(userRepository.findById(bidDTO.getUserId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found for ID: " + bidDTO.getUserId()))
                    .getName());
            System.out.println("the bidder is here in redis service "+bidResponseDTO.getBidder());
            bidResponseDTO.setAmount(bidDTO.getAmount());
            return bidResponseDTO;
        }).collect(Collectors.toSet());

    }

    @SuppressWarnings("unchecked")
    public void createAndSaveLeaderboard(Long auctionId, Set<BidResponseDTO> leaderboard) {
        String key = "auction:" + auctionId + ":leaderboard";

//        here we are skipping type checking so make sure you are figuring out the right thing
        Set<ZSetOperations.TypedTuple<Object>> redisTuples = new HashSet<>();

        for (BidResponseDTO bid : leaderboard) {
            redisTuples.add(new DefaultTypedTuple<>(bid, (double) bid.getAmount()));
        }

        redisTemplate.opsForZSet().add(key, redisTuples);
        redisTemplate.expire(key, 1, TimeUnit.HOURS);
    }

    public BidDTO getTopBidForKafka(Long auctionId) {
        String key = "auction:" + auctionId + ":leaderboard";
        Set<ZSetOperations.TypedTuple<Object>> topBids = redisTemplate.opsForZSet().reverseRangeWithScores(key, 0, 0);
        logger.info("Fetching top bids for auctionId: " + auctionId);

        if (topBids != null && !topBids.isEmpty()) {
            ZSetOperations.TypedTuple<Object> topBid = topBids.iterator().next();
            if (topBid != null && topBid.getValue() instanceof BidResponseDTO responseDTO) {

                // Convert to BidDTO manually
                BidDTO bidDTO = new BidDTO();
                bidDTO.setUserId(responseDTO.getBidderId());
                bidDTO.setAmount(responseDTO.getAmount());
                bidDTO.setAuctionId(auctionId); // if needed and not already set

                return bidDTO;
            } else {
                logger.warning("Top bid is not of type BidResponseDTO for auctionId: " + auctionId);
            }
        } else {
            logger.info("No bids found in Redis leaderboard for auctionId: " + auctionId);
        }

        return null;
    }


    public void deleteBidderFromLeaderboard(BidDTO bidDTO) {
        String key="auction:"+bidDTO.getAuctionId()+":leaderboard";
        // Remove the user's bid from the leaderboard
        redisTemplate.opsForZSet().remove(key, bidDTO.getUserId());
    }
}