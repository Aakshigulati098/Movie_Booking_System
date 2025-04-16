package com.example.movie_booking_system.KafkaConsumer;


import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.service.AuctionService;
import com.example.movie_booking_system.service.NotificationService;
import com.example.movie_booking_system.service.RedisService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;

@Service
public class ActionResultConsumer {

    @Value("${auction.kafka.topic.winner}")
    private  String WinnerLeaderboardTopic;

    private static final Logger logger = Logger.getLogger(ActionResultConsumer.class.getName());

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuctionService auctionService;
    @Autowired
    private RedisService redisService;


    @Autowired
    @Qualifier("stringKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @KafkaListener(topics = "${auction.kafka.topic.expired}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, AuctionResultDTO> record) {
        AuctionResultDTO result = record.value();
        logger.info("Received auction result: " + result.getAuctionId());
        try {

//            first persist the data in the db be it redis or mysql
//            write the logic to generate the zset leaderboard for redis although i will just call the redis service her e
            redisService.createAndSaveLeaderboard(result.getAuctionId(), result.getLeaderboard());
            logger.info("so i have persisted the leaderboard in redis for auction ID: " + result.getAuctionId());
            System.out.println("and now i would be producing a message to a new kafka topic which will be consumed by the notification service");
            // Produce a message to the new Kafka topic
                String key = "auction:" + result.getAuctionId() + ":leaderboard";


                CompletableFuture<SendResult<String,String>> future=kafkaTemplate.send(WinnerLeaderboardTopic,key,key);

                future.whenComplete((sendResult, ex) -> {
                    if (ex != null) {
                        logger.severe("Error sending message to Kafka for storing leaderboard: " + ex.getMessage());
                    } else {
                        logger.info("Message sent to Kafka topic: " + sendResult.getRecordMetadata().topic() +
                                ", partition: " + sendResult.getRecordMetadata().partition() +
                                ", offset: " + sendResult.getRecordMetadata().offset());
                    }
                });



//            then just fetch the top bid and send notification to the top bidder

//            so you know what the producer should be producing this to different topic i suppose!!
//            or else why not i produce this thing to a different topic and then consume it  !!
//            the flow would be here i will make a new entry to redis db which will act as our source of truth!!
//            and we will be having a new topic for this !!
//            in consuming of the new topic the consumer will receive the hash of redis !!
//            which he can use to fetch the top bid and execute the notification service which will just need auctioin details and bidder details !!

//            and we will remove the bidder details from the redis db such that the next bidder will be the top bidder next time
//            if the bidder rejects we will again produce the same to the topic and then consume it make sure you have access to the redis hash key here
//            and if the bidder accepts then we will have a seperate topic whose responsibilities will be
            //            payment handling
//            make sure the payment is success as if it is not success then we need to again do the same thing
//            db cleanup be it redis and db
//            db updation

//            ticket transfer

            // Update auction status in database
            auctionService.completeAuction(result);
            logger.info("Auction status updated in database for auction ID: " + result.getAuctionId());



            // Send notifications to participants
//            notificationService.notifyAuctionParticipants(result);
//            logger.info("Notifications sent for auction ID: " + result.getAuctionId());

        } catch (Exception e) {
            logger.severe("Error processing auction result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}