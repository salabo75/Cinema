package com.kursova.cinema.repositories;

import com.kursova.cinema.models.Screen;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ScreenRepository extends JpaRepository<Screen, Long> {
}
