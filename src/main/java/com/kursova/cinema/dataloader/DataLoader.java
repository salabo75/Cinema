package com.kursova.cinema.dataloader;

import com.kursova.cinema.models.Movie;
import com.kursova.cinema.models.Role;
import com.kursova.cinema.models.Screen;
import com.kursova.cinema.models.User;
import com.kursova.cinema.repositories.MovieRepository;
import com.kursova.cinema.repositories.RoleRepository;
import com.kursova.cinema.repositories.ScreenRepository;
import com.kursova.cinema.repositories.UserRepository;
import jakarta.transaction.Transactional;
import java.util.Set;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataLoader implements ApplicationRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final MovieRepository movieRepository;
    private final ScreenRepository screenRepository;

    public DataLoader(RoleRepository roleRepository, UserRepository userRepository, MovieRepository movieRepository,
                      ScreenRepository screenRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.movieRepository = movieRepository;
        this.screenRepository = screenRepository;
    }

    @Transactional
    public void run(ApplicationArguments args) {
        if (userRepository.findAll().size() == 0 || roleRepository.findAll().size() < 2) {
            roleRepository.save(new Role("ROLE_MANAGER"));
            roleRepository.save(new Role("ROLE_USER"));

            User defaultUserManager = new User();
            defaultUserManager.setUsername("manager");
            defaultUserManager.setPassword(new BCryptPasswordEncoder().encode("manager"));

            defaultUserManager.setRoles(Set.of(roleRepository.findByName("ROLE_MANAGER")));
            userRepository.save(defaultUserManager);

            Movie basicMovie = Movie.builder()
                .name("Name of movie")
                .durationTime("1:32:30")
                .rating("5")
                .actors("Actor1, Actor2, Actor3")
                .urlToImage("https://marketplace.canva.com/EAFH3gODxw4/1/0/1131w/canva-black-%26-white-modern-mystery-forest-movie-poster-rLty9dwhGG4.jpg")
                .build();
            movieRepository.save(basicMovie);

            Screen basicScreen = Screen.builder()
                .name("Screen A")
                .bronzeSeats(10)
                .silverSeats(8)
                .goldSeats(6)
                .build();
            screenRepository.save(basicScreen);
        }
    }
}
