package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionRepository extends JpaRepository<Auction, Long> {
    @Query("SELECT w FROM AuctionWinner w WHERE w.winnerId.id = :userId ")
    List<AuctionWinner> findPendingPaymentsByUserId(@Param("userId") Long userId);
}
