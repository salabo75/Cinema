package com.kursova.cinema.repositories;

import com.kursova.cinema.models.Movie;
import com.kursova.cinema.models.Seat;
import com.kursova.cinema.models.Show;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ShowRepository extends JpaRepository<Show, Long> {

    List<Show> findAllByMovie(Movie movie);
}
