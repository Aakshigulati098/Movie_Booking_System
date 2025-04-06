package com.example.movie_booking_system.repository;

import com.example.movie_booking_system.models.Bids;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface BidsRepository extends JpaRepository<Bids, Long> {
}
