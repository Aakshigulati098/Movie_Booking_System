package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Auction;
import com.example.movie_booking_system.models.AuctionWinner;
import com.example.movie_booking_system.models.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuctionWinnerRepository extends JpaRepository<AuctionWinner,Long> {
    AuctionWinner findByAuctionID(Auction auction);

    List<AuctionWinner> findAllByWinnerId(Users winnerId);
}
