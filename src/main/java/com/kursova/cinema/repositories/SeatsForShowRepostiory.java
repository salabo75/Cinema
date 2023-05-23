package com.kursova.cinema.repositories;

import com.kursova.cinema.models.SeatsForShow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SeatsForShowRepostiory extends JpaRepository<SeatsForShow, Long> {
}
