package com.example.movie_booking_system.service;

import com.example.movie_booking_system.dto.AuctionResultDTO;
import com.example.movie_booking_system.dto.BidDTO;
import com.example.movie_booking_system.dto.PendingAuctionDTO;
import com.example.movie_booking_system.dto.createAuctionDTO;
import com.example.movie_booking_system.models.*;
import com.example.movie_booking_system.repository.AuctionRepository;
import com.example.movie_booking_system.repository.AuctionWinnerRepository;
import com.example.movie_booking_system.repository.BookingRepository;
import com.example.movie_booking_system.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionServiceTest {

    @Mock
    private AuctionRepository auctionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private AuctionWinnerRepository auctionWinnerRepository;

    @Mock
    private KafkaTemplate<String, String> kafkaTemplate;

    @Mock
    private RedisService redisService;

    @Mock
    private BookingService bookingService;

    @Mock
    private WebSocketService webSocketService;

    @InjectMocks
    private AuctionService auctionService;

    @Captor
    private ArgumentCaptor<Auction> auctionCaptor;

    private Users testUser;
    private Booking testBooking;
    private Auction testAuction;
    private AuctionWinner testAuctionWinner;
    private Movie testMovie;
    private ShowTime testShowtime;
    private Theatre testTheatre;

    @BeforeEach
    void setUp() {
        // Set the Kafka topic value via reflection
        ReflectionTestUtils.setField(auctionService, "WinnerLeaderboardTopic", "test-topic");

        // Setup test data
        testUser = new Users();
        testUser.setId(1L);
        testUser.setName("Test User");
        testUser.setEmail("test@example.com");

        testMovie = new Movie();
        testMovie.setId(1L);
        testMovie.setTitle("Test Movie");

        testTheatre = new Theatre();
        testTheatre.setId(1L);
        testTheatre.setName("Test Theatre");

        testShowtime = new ShowTime();
        testShowtime.setId(1L);
        testShowtime.setTime("14:00");
        testShowtime.setTheatre(testTheatre);

        testBooking = new Booking();
        testBooking.setId(1L);
        testBooking.setMovie(testMovie);
        testBooking.setShowtime(testShowtime);
        testBooking.setSeatIds("A1,A2");

        testAuction = new Auction();
        testAuction.setId(1L);
        testAuction.setStatus(AuctionStatus.PENDING);
        testAuction.setCreatedAt(LocalDateTime.now());
        testAuction.setEndsAt(LocalDateTime.now().plusMinutes(60));
        testAuction.setMin_Amount(100L);
        testAuction.setSeller(testUser);
        testAuction.setBookingId(testBooking);

        testAuctionWinner = new AuctionWinner();
        testAuctionWinner.setId(1L);
        testAuctionWinner.setAuctionID(testAuction);
        testAuctionWinner.setWinnerId(testUser);
        testAuctionWinner.setAmount(150L);
    }

    @Test
    void createAuction_Success() {
        // Arrange
        createAuctionDTO dto = new createAuctionDTO();
        dto.setUserId(1L);
        dto.setBookingId(1L);
        dto.setMinAmount(100L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(bookingRepository.findById(1L)).thenReturn(Optional.of(testBooking));
        when(auctionRepository.save(any(Auction.class))).thenReturn(testAuction);

        // Act
        Long result = auctionService.createAuction(dto);

        // Assert
        assertEquals(1L, result);
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction capturedAuction = auctionCaptor.getValue();
        assertEquals(AuctionStatus.PENDING, capturedAuction.getStatus());
        assertEquals(100L, capturedAuction.getMin_Amount());
        assertEquals(testUser, capturedAuction.getSeller());
        assertEquals(testBooking, capturedAuction.getBookingId());
        verify(redisService).saveAuctionMetadata(eq(1L), eq("ACTIVE"), any(LocalDateTime.class));
        verify(redisService).createLeaderboard(1L);
    }

    @Test
    void getAllActiveAuctions_Success() {
        // Arrange
        Map<Long, Map<String, String>> expectedAuctions = new HashMap<>();
        Map<String, String> auctionData = new HashMap<>();
        auctionData.put("status", "ACTIVE");
        auctionData.put("endsAt", "2025-04-25T15:00:00");
        expectedAuctions.put(1L, auctionData);

        when(redisService.getAllActiveAuctions()).thenReturn(expectedAuctions);

        // Act
        Map<Long, Map<String, String>> result = auctionService.getAllActiveAuctions();

        // Assert
        assertEquals(expectedAuctions, result);
        verify(redisService).getAllActiveAuctions();
    }

    @Test
    void getAuctionById_Success() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));

        // Act
        Auction result = auctionService.getAuctionById(1L);

        // Assert
        assertEquals(testAuction, result);
        verify(auctionRepository).findById(1L);
    }

    @Test
    void getAuctionById_NotFound() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.getAuctionById(1L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Auction not found", exception.getReason());
    }

    @Test
    void completeAuction_WithWinningBid() {
        // Arrange
        BidDTO winningBid = new BidDTO();
        winningBid.setUserId(1L);
        winningBid.setAmount(150L);

        AuctionResultDTO resultDTO = new AuctionResultDTO();
        resultDTO.setAuctionId(1L);
        resultDTO.setWinningBid(winningBid);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        // Act
        auctionService.completeAuction(resultDTO);

        // Assert
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction capturedAuction = auctionCaptor.getValue();
        assertEquals(AuctionStatus.SOLD, capturedAuction.getStatus());
        assertEquals(testUser, capturedAuction.getWinner());
        assertEquals(150L, capturedAuction.getFinalAmount());
    }

    @Test
    void completeAuction_NoWinningBid() {
        // Arrange
        AuctionResultDTO resultDTO = new AuctionResultDTO();
        resultDTO.setAuctionId(1L);
        resultDTO.setWinningBid(null);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));

        // Act
        auctionService.completeAuction(resultDTO);

        // Assert
        verify(auctionRepository).save(auctionCaptor.capture());
        Auction capturedAuction = auctionCaptor.getValue();
        assertEquals(AuctionStatus.SOLD, capturedAuction.getStatus());
        assertNull(capturedAuction.getFinalAmount());
    }

    @Test
    void completeAuction_AuctionNotFound() {
        // Arrange
        AuctionResultDTO resultDTO = new AuctionResultDTO();
        resultDTO.setAuctionId(999L);

        when(auctionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        auctionService.completeAuction(resultDTO);

        // Assert
        verify(auctionRepository, never()).save(any(Auction.class));
    }

    @Test
    void completeAuction_UserNotFound() {
        // Arrange
        BidDTO winningBid = new BidDTO();
        winningBid.setUserId(999L);
        winningBid.setAmount(150L);

        AuctionResultDTO resultDTO = new AuctionResultDTO();
        resultDTO.setAuctionId(1L);
        resultDTO.setWinningBid(winningBid);

        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.completeAuction(resultDTO)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getPendingPayments_Success() {
        // Arrange
        List<AuctionWinner> auctionWinners = Collections.singletonList(testAuctionWinner);

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(auctionWinnerRepository.findAllByWinnerId(testUser)).thenReturn(auctionWinners);

        // Set end time to 40 minutes from now for "plenty" time status
        testAuction.setEndsAt(LocalDateTime.now().plusMinutes(40));

        // Act
        List<PendingAuctionDTO> result = auctionService.getPendingPayments(1L);

        // Assert
        assertEquals(1, result.size());
        PendingAuctionDTO dto = result.get(0);
        assertEquals(1L, dto.getId());
        assertEquals("pending", dto.getStatus());
        assertEquals(150L, dto.getBidAmount());
        assertEquals("Test Movie", dto.getMovieTitle());
        assertEquals("14:00", dto.getTime());
        assertEquals("A1,A2", dto.getSeats());
        assertEquals("Test Theatre", dto.getTheater());
        assertEquals(100L, dto.getOriginalPrice());
        assertEquals("Test User", dto.getSeller());
        assertEquals("plenty", dto.getTimeLeft());
        assertTrue(dto.getExpiresIn().matches("\\d{2}:\\d{2}"));
    }

    @Test
    void getPendingPayments_UserNotFound() {
        // Arrange
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.getPendingPayments(999L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("User not found", exception.getReason());
    }

    @Test
    void getTimeLeftStatus_Plenty() {
        // Arrange
        Duration timeLeft = Duration.ofMinutes(40);

        // Act
        String result = ReflectionTestUtils.invokeMethod(auctionService, "getTimeLeftStatus", timeLeft);

        // Assert
        assertEquals("plenty", result);
    }

    @Test
    void getTimeLeftStatus_Warning() {
        // Arrange
        Duration timeLeft = Duration.ofMinutes(20);

        // Act
        String result = ReflectionTestUtils.invokeMethod(auctionService, "getTimeLeftStatus", timeLeft);

        // Assert
        assertEquals("warning", result);
    }

    @Test
    void getTimeLeftStatus_Critical() {
        // Arrange
        Duration timeLeft = Duration.ofMinutes(5);

        // Act
        String result = ReflectionTestUtils.invokeMethod(auctionService, "getTimeLeftStatus", timeLeft);

        // Assert
        assertEquals("critical", result);
    }

    @Test
    void handleRejection_Success() throws ExecutionException, InterruptedException {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        SendResult<String, String> sendResult = mock(SendResult.class);
        org.apache.kafka.clients.producer.RecordMetadata recordMetadata = mock(org.apache.kafka.clients.producer.RecordMetadata.class);

        when(recordMetadata.topic()).thenReturn("test-topic");
        when(recordMetadata.partition()).thenReturn(0);
        when(recordMetadata.offset()).thenReturn(0L);
        when(sendResult.getRecordMetadata()).thenReturn(recordMetadata);
        future.complete(sendResult);

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        MockTransactionSynchronizationManager.setup();

        // Act
        auctionService.handleRejection(1L);

        // Assert
        verify(auctionRepository).findById(1L);
        verify(kafkaTemplate).send(eq("test-topic"), eq("auction:1:leaderboard"), eq("auction:1:leaderboard"));

        // Run after-commit callbacks to verify WebSocket call
        MockTransactionSynchronizationManager.runAfterCommitCallbacks();
        verify(webSocketService).sendAuctionAcceptanceUpdates("Auction 1 was rejected");
    }

    @Test
    void handleRejection_AuctionNotFound() {
        // Arrange
        when(auctionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.handleRejection(999L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Auction not found for ID: 999", exception.getReason());
    }

    @Test
    void handleRejection_KafkaError() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.handleRejection(1L)
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to process auction rejection", exception.getReason());
    }

    @Test
    void handleAcceptance_Success() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));
        MockTransactionSynchronizationManager.setup();

        // Act
        auctionService.handleAcceptance(1L, 2L);

        // Assert
        verify(auctionRepository).findById(1L);
        assertEquals(AuctionStatus.SOLD, testAuction.getStatus());
        verify(bookingService).TransferBooking(eq(1L), eq(2L),  anyLong());
        verify(auctionRepository).delete(testAuction);

        // Run after-commit callbacks to verify WebSocket call
        MockTransactionSynchronizationManager.runAfterCommitCallbacks();
        verify(webSocketService).sendAuctionAcceptanceUpdates("Auction accepted successfully");
    }

    @Test
    void handleAcceptance_AuctionNotFound() {
        // Arrange
        when(auctionRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.handleAcceptance(999L, 2L)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("Auction not found", exception.getReason());
    }


    @Test
    void handleAcceptance_BookingTransferError() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));
        doThrow(new RuntimeException("Booking transfer error")).when(bookingService).TransferBooking(anyLong(), anyLong(), anyLong());

        // Act & Assert
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                auctionService.handleAcceptance(1L, 2L)
        );
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatusCode());
        assertEquals("Failed to process auction acceptance", exception.getReason());
    }

    @Test
    void handleRejection_FinallyBlockExecuted() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));

        CompletableFuture<SendResult<String, String>> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException("Kafka error"));

        when(kafkaTemplate.send(anyString(), anyString(), anyString())).thenReturn(future);

        // Act
        try {
            auctionService.handleRejection(1L);
        } catch (Exception ignored) {
        }

        // Assert
        verify(auctionRepository).findById(1L);
        verify(kafkaTemplate).send(anyString(), anyString(), anyString());
        verifyNoInteractions(webSocketService); // WebSocket should not be called on failure
    }
    @Test
    void handleAcceptance_WebSocketFailure() {
        // Arrange
        when(auctionRepository.findById(1L)).thenReturn(Optional.of(testAuction));
        doThrow(new RuntimeException("WebSocket error")).when(webSocketService).sendAuctionAcceptanceUpdates(anyString());

        MockTransactionSynchronizationManager.setup();

        // Act
        auctionService.handleAcceptance(1L, 2L);

        // Assert
        verify(auctionRepository).findById(1L);
        verify(bookingService).TransferBooking(eq(1L), eq(2L), anyLong());
        verify(auctionRepository).delete(testAuction);

        // Run after-commit callbacks to verify WebSocket call
        MockTransactionSynchronizationManager.runAfterCommitCallbacks();
        verify(webSocketService).sendAuctionAcceptanceUpdates("Auction accepted successfully");
    }
    /**
     * Helper class to mock TransactionSynchronizationManager behavior
     */
    private static class MockTransactionSynchronizationManager {
        private static List<TransactionSynchronization> synchronizations = new ArrayList<>();

        public static void setup() {
            synchronizations.clear();
            MockedStatic<org.springframework.transaction.support.TransactionSynchronizationManager> mockedStatic =
                    Mockito.mockStatic(org.springframework.transaction.support.TransactionSynchronizationManager.class);

            mockedStatic.when(() -> org.springframework.transaction.support.TransactionSynchronizationManager.registerSynchronization(any()))
                    .thenAnswer(invocation -> {
                        synchronizations.add(invocation.getArgument(0));
                        return null;
                    });
        }

        public static void runAfterCommitCallbacks() {
            for (TransactionSynchronization sync : synchronizations) {
                sync.afterCommit();
            }
        }
    }


}