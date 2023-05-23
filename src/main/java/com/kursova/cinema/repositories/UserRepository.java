package com.kursova.cinema.repositories;

import com.kursova.cinema.models.Seat;
import com.kursova.cinema.models.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);

    User findByBookedSeatsContaining(Seat seat);
}

