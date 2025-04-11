package com.example.movie_booking_system.KafkaConsumer;


import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.repository.UserRepository;
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

    @Autowired
    private RedisService redisService;


    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;




    @KafkaListener(topics = "${auction.kafka.topic.winner}", groupId = "${spring.kafka.consumer.group-id}",containerFactory = "stringKafkaListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        System.out.println("hey i am here in leaderboard consumer");
        String message = record.value();
        System.out.println("Received message: " + message);

//        mujhe idhar kya karna hai

//        top bidder find karo redis ke service se using the hash which is here the key

        Long auctionId = extractAuctionId(record.key());
        System.out.println("auction id here in winnerNotifyConsumer is: "+auctionId);
        BidDTO topBidder=redisService.getTopBidForKafka(auctionId);

//        hey handle the null case here
        if(topBidder==null) {
            System.out.println("no top bidder found");
            logger.info("no top bidder found so need to handle this flow in a different manner ");
//            here i need to mark the db status to unsold and then do further operations
//            like cleanup and everything and then actually get the best code possible in terms
//            of flow and the frontend that i am very much sceptical about
            return;
        }
        logger.info("i have got the top bidder from redis for auction ID: "+topBidder.getAuctionId());
//        usi bande ko redis ke leaderboard se hatao that thing you need to do keeping in mind the edge cases of
//        handling the nullable
//        and then usi bande ko notify karo
        notificationService.sendNotification(topBidder);
        logger.info("Top bidder has been sent notification to process the acceptance of the auction "+userRepository.findById(topBidder.getUserId()).get().getName());
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
