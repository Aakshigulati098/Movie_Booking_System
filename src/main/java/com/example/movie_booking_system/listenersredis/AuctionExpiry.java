
package com.example.movie_booking_system.listenersredis;


import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.BidResponseDTO;
import com.example.movie_booking_system.service.RedisService;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AuctionExpiry implements MessageListener {

    private static final Logger logger = Logger.getLogger(AuctionExpiry.class.getName());
    private static final Pattern AUCTION_KEY_PATTERN = Pattern.compile("auction(\\d+)");

    @Value("${auction.kafka.topic.expired}")
    private  String expiredAuctionTopic;


    private RedisMessageListenerContainer redisMessageListenerContainer;
    private KafkaTemplate<String, Object> kafkaTemplate;
    private RedisService redisService;

    @Autowired
    public AuctionExpiry(RedisMessageListenerContainer redisMessageListenerContainer,KafkaTemplate<String, Object> kafkaTemplate,RedisService redisService) {
        this.redisMessageListenerContainer = redisMessageListenerContainer;
        this.kafkaTemplate = kafkaTemplate;
        this.redisService = redisService;
    }

    @PostConstruct
    public void init() {
        // Subscribe to expired key events for auction keys
        redisMessageListenerContainer.addMessageListener(this,
                new PatternTopic("__keyevent@*__:expired"));
        logger.info("Registered listener for Redis key expiration events");
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String expiredKey = new String(message.getBody());
        logger.info("Received expiry event for key: " + expiredKey);

        // Process only auction metadata keys (not bid keys)
        if (expiredKey.startsWith("auction") && !expiredKey.contains(":")) {
            Matcher matcher = AUCTION_KEY_PATTERN.matcher(expiredKey);
            if (matcher.find()) {
                Long auctionId = Long.parseLong(matcher.group(1));
                handleAuctionExpiry(auctionId);
            }
        }
    }

    private void handleAuctionExpiry(Long auctionId) {
        logger.info("Processing expired auction: " + auctionId);

        try {
            // Get the winning bid
            BidDTO winningBid = redisService.getTopBid(auctionId);

            if (winningBid != null) {
                // Create auction result
                AuctionResultDTO result = new AuctionResultDTO();
                result.setAuctionId(auctionId);
                result.setWinningBid(winningBid);
                result.setLeaderboard(redisService.getLeaderboard(auctionId));

                logger.info("the winning bid here is "+result.getWinningBid());
                logger.info("the auction id here is "+result.getAuctionId());

                Set<BidResponseDTO> leaderboard = result.getLeaderboard();

// Convert Set to List for sorting
                List<BidResponseDTO> sortedLeaderboard = new ArrayList<>(leaderboard);

// Sort by amount (assuming higher bids should come first)
                sortedLeaderboard.sort((bid1, bid2) ->
                    bid2.getAmount().compareTo(bid1.getAmount()) // Descending order
                );
                for (BidResponseDTO bid : sortedLeaderboard) {
                    logger.info("BidderId : " + bid.getBidderId()+ ", Bidder : " + bid.getBidder() + ", Amount: " + bid.getAmount()+ ", Auction ID: " + bid.getAuctionId());
                }

                // Publish result to Kafka
                logger.info("Publishing auction result to Kafka for auction: " + auctionId);

                CompletableFuture<SendResult<String, Object>> future = kafkaTemplate.send(expiredAuctionTopic, auctionId.toString(), result);

                future.whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        logger.severe("Failed to publish auction result for auctionId: " + auctionId + ", error: " + ex.getMessage());
                        ex.printStackTrace();
                    } else {
                        logger.info("Successfully published auction result to Kafka for auctionId: " + auctionId);
                    }
                });
            } else {
                logger.info("No bids found for expired auction: " + auctionId);

                // Send a notification about the auction ending with no bids
                AuctionResultDTO result = new AuctionResultDTO();
                result.setAuctionId(auctionId);
                result.setNoBids(true);

                kafkaTemplate.send(expiredAuctionTopic, auctionId.toString(), result);
            }
        } catch (Exception e) {
            logger.severe("Error processing expired auction " + auctionId + ": " + e.getMessage());
            e.printStackTrace();
        }
    }
}