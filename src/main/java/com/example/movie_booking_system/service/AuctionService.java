package com.example.movie_booking_system.service;

import com.example.movie_booking_system.KafkaConsumer.ActionResultConsumer;
import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.dto.PendingAuctionDTO;
import com.example.movie_booking_system.dto.createAuctionDTO;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionStatus;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import com.example.movie_booking_system.repository.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Service
public class AuctionService {

    @PersistenceContext
    private EntityManager entityManager;

    @Autowired
    private AuctionRepository auctionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    @Qualifier("stringKafkaTemplate")
    private KafkaTemplate<String, String> kafkaTemplate;

    @Value("${auction.kafka.topic.winner}")
    private  String WinnerLeaderboardTopic;

    @Autowired
    private AuctionWinnerRepository auctionWinnerRepository;



    @Autowired
    private BookingRepository bookingRepository;

    private static final Logger logger = Logger.getLogger(AuctionService.class.getName());
    @Autowired
    private RedisService redisService;
    @Autowired
    private BookingService bookingService;
    @Autowired
    private WebSocketService webSocketService;

    public Long createAuction(createAuctionDTO Incomingauction) throws ResponseStatusException {
        // Check if the bookingId is valid to be created as an auction
        boolean isValid = true; // Here I will be calling a function with the bookingId

        if (isValid) {
            // Create a new auction
            Auction auction = new Auction();
            auction.setStatus(AuctionStatus.PENDING);
            auction.setCreatedAt(LocalDateTime.now());
            auction.setMin_Amount(Incomingauction.getMinAmount());
            auction.setSeller(userRepository.findById(Incomingauction.getUserId()).orElse(null));
            // Set the end time to 1 hour from now
            auction.setEndsAt(LocalDateTime.now().plusSeconds(120)); //i have set this auction for 2 minutes
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

    @Transactional
    public void completeAuction(AuctionResultDTO result) {
        System.out.println("Completing auction: " + result.getAuctionId());

        Optional<Auction> auctionOpt = auctionRepository.findById(result.getAuctionId());
        if (auctionOpt.isEmpty()) {
            System.out.println("Auction not found: " + result.getAuctionId());
            return;
        }

        AuctionStatus status = AuctionStatus.SOLD;
        Auction auction = auctionOpt.get();
        auction.setStatus(status);

        if (result.getWinningBid() != null) {
            Users userr= userRepository.findById(result.getWinningBid().getUserId())
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
            auction.setWinner(userr);
            auction.setFinalAmount(result.getWinningBid().getAmount());
        }

        auctionRepository.save(auction);

//        System.out.println("Auction " +getAuctionId() + " marked as completed");
        System.out.println("Auction " + result.getAuctionId() + " marked as completed");
//        some operations are still left so need to configure that thing first
    }



    public List<PendingAuctionDTO> getPendingPayments(Long userId) {
//        List<AuctionWinner> auctionWinners = auctionRepository.findPendingPaymentsByUserId(userId);
        Users user = userRepository.findById(userId).orElse(null);
        if(user==null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        List<AuctionWinner> auctionWinners = auctionWinnerRepository.findAllByWinnerId(user);
        logger.info("i am in getPendingPayments method of auction service");
        return auctionWinners.stream()
                .map(auctionWinner -> {
                    Auction auction = auctionWinner.getAuctionID();
                    PendingAuctionDTO dto = new PendingAuctionDTO();

                    dto.setId(auction.getId());
                    dto.setStatus("pending");
                    dto.setBidAmount(auctionWinner.getAmount());
                    dto.setMovieTitle(auction.getBookingId().getMovie().getTitle());
                    dto.setTime(auction.getBookingId().getShowtime().getTime());
                    dto.setSeats(auction.getBookingId().getSeatIds());
                    dto.setTheater(auction.getBookingId().getShowtime().getTheatre().getName());
                    dto.setOriginalPrice(auction.getMin_Amount());
                    dto.setSeller(auction.getSeller().getName());

                    LocalDateTime now = LocalDateTime.now();
                    Duration timeLeft = Duration.between(now, auction.getEndsAt());
                    dto.setExpiresIn(String.format("%02d:%02d", timeLeft.toMinutes(), timeLeft.toSecondsPart()));
                    dto.setTimeLeft(getTimeLeftStatus(timeLeft));

                    return dto;
                })
                .collect(Collectors.toList());
    }

    private String getTimeLeftStatus(Duration timeLeft) {
        long minutesLeft = timeLeft.toMinutes();
        if (minutesLeft > 30) {
            return "plenty";
        } else if (minutesLeft > 10) {
            return "warning";
        } else {
            return "critical";
        }
    }

    @Transactional
    public void handleRejection(Long auctionId) {
        logger.info("Processing rejection for auction ID: " + auctionId);

        // Validate auction exists
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Auction not found for ID: " + auctionId));

        try {
            // Prepare leaderboard key
            String leaderboardKey = "auction:" + auctionId + ":leaderboard";

            // Send to Kafka
            CompletableFuture<SendResult<String, String>> future = kafkaTemplate
                    .send(WinnerLeaderboardTopic, leaderboardKey, leaderboardKey);

            future.whenComplete((sendResult, ex) -> {
                if (ex != null) {
                    logger.severe("Failed to send Kafka message for auction " + auctionId + ": " + ex.getMessage());
                    throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                            "Failed to process auction rejection");
                }

                // Log Kafka success
                logger.info("Kafka message sent for auction rejection. Topic: {}, Partition: {}, Offset: {}"+
                        sendResult.getRecordMetadata().topic()+
                        sendResult.getRecordMetadata().partition()+
                        sendResult.getRecordMetadata().offset());

                // Send WebSocket notification
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        webSocketService.sendAuctionAcceptanceUpdates("Auction " + auctionId + " was rejected");
                        logger.info("WebSocket notification sent for auction rejection: " + auctionId);
                    }
                });

            });

        } catch (Exception e) {
            logger.severe("Error in handleRejection for auction " + auctionId + ": " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Failed to process auction rejection");
        }

        logger.info("Rejection processing completed for auction ID: " + auctionId);
    }

    @Transactional
    public void handleAcceptance(Long auctionId, Long userId) {
        logger.info("Processing acceptance for auction ID: " + auctionId);

        // Find auction and validate
        Auction auction = auctionRepository.findById(auctionId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Auction not found"));

        try {
            // Update auction status
            auction.setStatus(AuctionStatus.SOLD);
            logger.info("Updated auction status to SOLD for auction ID: " + auctionId);

            // Transfer booking
            bookingService.TransferBooking(auction.getBookingId().getId(), userId, auction.getFinalAmount());
            logger.info("Booking transferred successfully for auction ID: " + auctionId);

            // Delete auction (cascading will handle bids and winner)
            auctionRepository.delete(auction);
            logger.info("Deleted auction and related entities for auction ID: " + auctionId);

            // Send WebSocket notification
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    webSocketService.sendAuctionAcceptanceUpdates("Auction accepted successfully");
                    logger.info("Sent WebSocket notification for auction acceptance");
                }
            });



        } catch (Exception e) {
            logger.severe("Error in handleAcceptance: " + e.getMessage());
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to process auction acceptance");
        }
    }

}