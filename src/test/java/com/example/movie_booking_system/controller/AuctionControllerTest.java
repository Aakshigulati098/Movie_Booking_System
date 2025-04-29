package com.example.movie_booking_system.controller;

import com.example.movie_booking_system.dto.*;
import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.service.AuctionService;
import com.example.movie_booking_system.service.BidHandlerService;
import com.example.movie_booking_system.service.RedisService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuctionControllerTest {

    @Mock
    private AuctionService auctionService;

    @Mock
    private BidHandlerService bidHandlerService;

    @Mock
    private RedisService redisService;

    @InjectMocks
    private AuctionController auctionController;

    @Test
    void hello_ShouldReturnHelloMessage() {
        String response = auctionController.hello();
        assertEquals("hello i am from auction ", response);
    }

    @Test
    void getAllActiveAuctions_ShouldReturnActiveAuctions() {
        Map<Long, Map<String, String>> activeAuctions = new HashMap<>();
        activeAuctions.put(1L, Map.of("key", "value"));
        when(redisService.getAllActiveAuctions()).thenReturn(activeAuctions);

        ResponseEntity<Map<Long, Map<String, String>>> response = auctionController.getAllActiveAuctions();

        assertNotNull(response);
        assertEquals(activeAuctions, response.getBody());
        verify(redisService, times(1)).getAllActiveAuctions();
    }

    @Test
    void createAuction_ShouldReturnSuccessMessage() {
        CreateAuctionDTO auctionDTO = new CreateAuctionDTO(1L, 1L, 1L, 100L);
        auctionDTO.setUserId(1L);
        when(auctionService.createAuction(auctionDTO)).thenReturn(1L);

        ResponseEntity<Object> response = auctionController.createAuction(auctionDTO);

        assertNotNull(response);
        assertEquals("Auction created successfully with ID: 1", response.getBody());
        verify(auctionService, times(1)).createAuction(auctionDTO);
    }

    @Test
    void createAuction_ShouldHandleException() {
        CreateAuctionDTO auctionDTO = new CreateAuctionDTO(1L, 1L, 1L, 100L);
        when(auctionService.createAuction(auctionDTO)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = auctionController.createAuction(auctionDTO);

        assertNotNull(response);
        assertEquals("Auction creation failed Error", response.getBody());
        verify(auctionService, times(1)).createAuction(auctionDTO);
    }

    @Test
    void handleBid_ShouldReturnSuccessMessage() {
        BidDTO bidDTO = new BidDTO();
        when(bidHandlerService.handleBid(1L, bidDTO)).thenReturn(true);

        ResponseEntity<Object> response = auctionController.handleBid(1L, bidDTO);

        assertNotNull(response);
        assertEquals("Bid placed successfully", response.getBody());
        verify(bidHandlerService, times(1)).handleBid(1L, bidDTO);
    }

    @Test
    void handleBid_ShouldReturnFailureMessage() {
        BidDTO bidDTO = new BidDTO();
        when(bidHandlerService.handleBid(1L, bidDTO)).thenReturn(false);

        ResponseEntity<Object> response = auctionController.handleBid(1L, bidDTO);

        assertNotNull(response);
        assertEquals("Bid placement failed", response.getBody());
        verify(bidHandlerService, times(1)).handleBid(1L, bidDTO);
    }

    @Test
    void handleBid_ShouldHandleException() {
        BidDTO bidDTO = new BidDTO();
        when(bidHandlerService.handleBid(1L, bidDTO)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = auctionController.handleBid(1L, bidDTO);

        assertNotNull(response);
        assertEquals("Bid handling failed Error", response.getBody());
        verify(bidHandlerService, times(1)).handleBid(1L, bidDTO);
    }

    @Test
    void getLeaderboard_ShouldReturnLeaderboard() {
        Set<BidResponseDTO> leaderboard = new HashSet<>();
        when(redisService.getLeaderboard(1L)).thenReturn(leaderboard);

        ResponseEntity<Set<BidResponseDTO>> response = auctionController.getLeaderboard(1L);

        assertNotNull(response);
        assertEquals(leaderboard, response.getBody());
        verify(redisService, times(1)).getLeaderboard(1L);
    }

    @Test
    void getActiveAuctions_ShouldReturnActiveAuctions() {
        Map<Long, Map<String, String>> activeAuctions = new HashMap<>();
        Auction auction = new Auction();
        auction.setId(1L);
        lenient().when(redisService.getAllActiveAuctions()).thenReturn(activeAuctions);
        lenient().when(auctionService.getAuctionById(1L)).thenReturn(auction);

        ResponseEntity<List<AuctionResponseDTO>> response = auctionController.getActiveAuctions();

        assertNotNull(response);
        verify(redisService, times(1)).getAllActiveAuctions();
    }


    @Test
    void getAuctionDetails_ShouldThrowExceptionIfNotFound() {
        when(auctionService.getAuctionById(1L)).thenReturn(null);

        assertThrows(ResponseStatusException.class, () -> auctionController.getAuctionDetails(1L));
        verify(auctionService, times(1)).getAuctionById(1L);
    }

    @Test
    void getPendingPayments_ShouldReturnPendingPayments() {
        List<PendingAuctionDTO> pendingPayments = new ArrayList<>();
        when(auctionService.getPendingPayments(1L)).thenReturn(pendingPayments);

        ResponseEntity<Object> response = auctionController.getPendingPayments(1L);

        assertNotNull(response);
        assertEquals(pendingPayments, response.getBody());
        verify(auctionService, times(1)).getPendingPayments(1L);
    }

    @Test
    void getPendingPayments_ShouldHandleException() {
        when(auctionService.getPendingPayments(1L)).thenThrow(new RuntimeException("Error"));

        ResponseEntity<Object> response = auctionController.getPendingPayments(1L);

        assertNotNull(response);
        assertEquals("Error fetching pending payments: Error", response.getBody());
        verify(auctionService, times(1)).getPendingPayments(1L);
    }

    @Test
    void handleAuctionWinRejectResponse_ShouldReturnSuccessMessage() {
        doNothing().when(auctionService).handleRejection(1L);

        ResponseEntity<Object> response = auctionController.handleAuctionWinRejectResponse(1L, 1L);

        assertNotNull(response);
        assertEquals("Response recorded successfully", response.getBody());
        verify(auctionService, times(1)).handleRejection(1L);
    }

    @Test
    void handleAuctionWinRejectResponse_ShouldHandleException() {
        doThrow(new RuntimeException("Error")).when(auctionService).handleRejection(1L);

        ResponseEntity<Object> response = auctionController.handleAuctionWinRejectResponse(1L, 1L);

        assertNotNull(response);
        assertEquals("Error handling auction win response: Error", response.getBody());
        verify(auctionService, times(1)).handleRejection(1L);
    }

    @Test
    void handleAuctionWinAcceptResponse_ShouldReturnSuccessMessage() {
        doNothing().when(auctionService).handleAcceptance(1L, 1L);

        ResponseEntity<Object> response = auctionController.handleAuctionWinAcceptResponse(1L, 1L);

        assertNotNull(response);
        assertEquals("Response recorded successfully", response.getBody());
        verify(auctionService, times(1)).handleAcceptance(1L, 1L);
    }

    @Test
    void handleAuctionWinAcceptResponse_ShouldHandleException() {
        doThrow(new RuntimeException("Error")).when(auctionService).handleAcceptance(1L, 1L);

        ResponseEntity<Object> response = auctionController.handleAuctionWinAcceptResponse(1L, 1L);

        assertNotNull(response);
        assertEquals("Error handling auction win response: Error", response.getBody());
        verify(auctionService, times(1)).handleAcceptance(1L, 1L);
    }
}