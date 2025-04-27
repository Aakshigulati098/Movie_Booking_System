package com.example.movie_booking_system.kafkaconsumer;


import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionStatus;
import com.example.movie_booking_system.repository.AuctionRepository;

import com.example.movie_booking_system.service.NotificationService;
import com.example.movie_booking_system.service.RedisService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;


@Service
public class WinnerNotifyConsumer {


    private static final Logger logger = Logger.getLogger(WinnerNotifyConsumer.class.getName());


    private RedisService redisService;
    private NotificationService notificationService;

    private AuctionRepository auctionRepository;

    @Autowired
    public WinnerNotifyConsumer(RedisService redisService, NotificationService notificationService,  AuctionRepository auctionRepository) {
        this.redisService = redisService;
        this.notificationService = notificationService;

        this.auctionRepository = auctionRepository;
    }




    @KafkaListener(topics = "${auction.kafka.topic.winner}", groupId = "${spring.kafka.consumer.group-id}",containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> myRecord) {
        logger.info("hey i am here in leaderboard consumer");
        String message = myRecord.value();
        logger.info("Received message: " + message);

//        mujhe idhar kya karna hai

//        top bidder find karo redis ke service se using the hash which is here the key

        Long auctionId = extractAuctionId(myRecord.key());
       logger.info("auction id here in winnerNotifyConsumer is: "+auctionId);
        BidDTO topBidder=redisService.getTopBidForKafka(auctionId);

//        hey handle the null case here
        if(topBidder==null) {
            logger.info("no top bidder found");
            logger.info("no top bidder found so need to handle this flow in a different manner ");
//            here i need to mark the db status to unsold and then do further operations
            Auction auction=auctionRepository.findById(auctionId).orElse(null);
            if(auction==null) {
                logger.info("auction not found in the db for auction ID: "+auctionId);
                return;
            }
            auction.setStatus(AuctionStatus.UNSOLD);
            logger.info("auction status has been set to unsold for auction ID: "+auctionId);
//            like cleanup and everything and then actually get the best code possible in terms
//            of flow and the frontend that i am very much sceptical about
            return;
        }
        logger.info("i have got the top bidder from redis for auction ID: "+topBidder.getUserId());

        redisService.deleteBidderFromLeaderboard(topBidder);

//        and then usi bande ko notify karo
        notificationService.sendNotification(topBidder);

    }

    private Long extractAuctionId(String key) {
        String[] parts = key.split(":");
        if (parts.length >= 2) {
            return Long.parseLong(parts[1]);
        } else {
            throw new IllegalArgumentException("Invalid auction key format: " + key);
        }
    }
}
