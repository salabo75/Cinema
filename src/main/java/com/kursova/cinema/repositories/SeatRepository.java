package com.kursova.cinema.repositories;

import com.kursova.cinema.models.Seat;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatRepository extends JpaRepository<Seat, Long> {
}
