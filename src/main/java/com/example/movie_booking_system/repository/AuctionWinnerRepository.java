package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuctionWinnerRepository extends JpaRepository<AuctionWinner,Long> {
    @Query("SELECT aw FROM AuctionWinner aw WHERE aw.auctionID = :auction")
    Optional<AuctionWinner> findByAuctionID(@Param("auction") Auction auction); // Changed from auctionID to auctionId

    List<AuctionWinner> findAllByWinnerId(Users winnerId);
}
