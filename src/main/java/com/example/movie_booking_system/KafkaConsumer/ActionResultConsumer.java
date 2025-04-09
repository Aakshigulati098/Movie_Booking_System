package com.example.movie_booking_system.KafkaConsumer;


import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.service.AuctionService;
import com.example.movie_booking_system.service.NotificationService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.logging.Logger;

@Service
public class ActionResultConsumer {

    private static final Logger logger = Logger.getLogger(ActionResultConsumer.class.getName());

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuctionService auctionService;

    @KafkaListener(topics = "${auction.kafka.topic.expired}", groupId = "${spring.kafka.consumer.group-id}")
    public void consume(ConsumerRecord<String, AuctionResultDTO> record) {
        AuctionResultDTO result = record.value();
        logger.info("Received auction result: " + result);

        try {
            // Update auction status in database
            auctionService.completeAuction(result);

            // Send notifications to participants
            notificationService.notifyAuctionParticipants(result);

        } catch (Exception e) {
            logger.severe("Error processing auction result: " + e.getMessage());
            e.printStackTrace();
        }
    }
}