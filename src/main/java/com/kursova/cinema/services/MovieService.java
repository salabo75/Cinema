package com.kursova.cinema.services;

import com.kursova.cinema.models.Movie;
import com.kursova.cinema.repositories.MovieRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> findAll(){return movieRepository.findAll();}

    public void save(Movie movie){movieRepository.save(movie);}

    public Optional<Movie> findById(Long id) {return movieRepository.findById(id);}

    public void delete(Movie movie){
        movieRepository.delete(movie);
    }
}
