package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuctionWinnerRepository extends JpaRepository<AuctionWinner,Long> {
    Optional<AuctionWinner> findByAuctionID(Auction auction);
}
