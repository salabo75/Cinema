package com.kursova.cinema.controllers;

import com.kursova.cinema.DTOs.ShowFilmDTO;
import com.kursova.cinema.DTOs.UserSeatDTO;
import com.kursova.cinema.models.Movie;
import com.kursova.cinema.models.Screen;
import com.kursova.cinema.models.Seat;
import com.kursova.cinema.models.SeatsForShow;
import com.kursova.cinema.models.Show;
import com.kursova.cinema.models.User;
import com.kursova.cinema.repositories.RoleRepository;
import com.kursova.cinema.repositories.ScreenRepository;
import com.kursova.cinema.repositories.SeatRepository;
import com.kursova.cinema.repositories.SeatsForShowRepostiory;
import com.kursova.cinema.repositories.ShowRepository;
import com.kursova.cinema.services.MovieService;
import com.kursova.cinema.services.UserService;
import jakarta.transaction.Transactional;
import java.security.Principal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class MainController {
    private final UserService userService;
    private final MovieService movieService;
    private final ScreenRepository screenRepository;
    private final ShowRepository showRepository;
    private final SeatRepository seatRepository;
    private final RoleRepository roleRepository;
    private final SeatsForShowRepostiory seatsForShowRepostiory;

    public MainController(UserService userService, MovieService movieService, ScreenRepository screenRepository,
                          ShowRepository showRepository, SeatRepository seatRepository, RoleRepository roleRepository,
                          SeatsForShowRepostiory seatsForShowRepostiory) {
        this.userService = userService;
        this.movieService = movieService;
        this.screenRepository = screenRepository;
        this.showRepository = showRepository;
        this.seatRepository = seatRepository;
        this.roleRepository = roleRepository;
        this.seatsForShowRepostiory = seatsForShowRepostiory;
    }

    @GetMapping({"/", "/home"})
    public String homePage(Principal principal, Model model) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        List<Movie> moviesList = movieService.findAll();
        List<Show> showList = showRepository.findAll();
        List<ShowFilmDTO> showFilmDTOList = new ArrayList<>();
        for (Movie m : moviesList
        ) {
            ShowFilmDTO dto = new ShowFilmDTO();
            dto.setMovie(m);
            for (Show s : showList
            ) {
                if (s.getMovie().equals(m)) {
                    dto.addShow(s);
                }
            }
            showFilmDTOList.add(dto);
        }
        model.addAttribute("movies", showFilmDTOList);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "index";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model) {
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/register")
    public String register(Model model) {
        return "login";
    }

    @PostMapping("/register")
    public String createUser(Model model, @RequestParam String regName, @RequestParam String regPass) {
        User foundPerson = userService.findByUsername(regName).orElse(null);

        if (foundPerson != null || regName.length() < 4 || regPass.length() < 4) {
            model.addAttribute("registerError", true);
            return "login";
        }
        User newUser = User.builder()
            .username(regName)
            .password(regPass)
            .build();

        userService.addUser(newUser);
        return "redirect:/login";
    }

    @GetMapping("/logout")
    public String logout() {
        return "login";
    }

    @GetMapping("/accessDenied")
    public String accessDenied() {
        return "redirect:/";
    }

    @GetMapping("/add-movie")
    public String addFilm(Principal principal, Model model) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }
        Movie movie = new Movie();
        model.addAttribute("movie", movie);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "add-film";
    }

    @PostMapping("/save-movie")
    public String saveFilm(@ModelAttribute(name = "movie") Movie movie, Model model) {
        if (!movie.getDurationTime().isEmpty() && !movie.getName().isEmpty() && !movie.getActors().isEmpty() &&
            !movie.getRating().isEmpty() && !movie.getUrlToImage().isEmpty()) {
            movieService.save(movie);
        }
        return "redirect:/";
    }

    @GetMapping("/movies-list")
    public String moviesList(Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }
        List<Movie> movieList = movieService.findAll();
        model.addAttribute("movieList", movieList);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "movies-list";
    }

    @GetMapping("/shows-list")
    public String showsList(Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }
        List<Show> allShows = showRepository.findAll();
        model.addAttribute("allShows", allShows);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "shows-list";
    }

    @PostMapping("/delete-show/{id}")
    @Transactional
    public String deleteShow(@PathVariable Long id, Model model) {
        Optional<Show> show = showRepository.findById(id);
        if (show.isEmpty()) {
            return "redirect:/";
        }
        Show showToDelete = show.get();
        SeatsForShow seatsForShow = showToDelete.getSeatsForShow();
        List<Seat> seatsToDelete = new ArrayList<>();
        seatsToDelete.addAll(seatsForShow.getGoldSeats());
        seatsToDelete.addAll(seatsForShow.getBronzeSeats());
        seatsToDelete.addAll(seatsForShow.getSilverSeats());
        List<User> users = userService.findAll();
        for (User u : users
        ) {
            List<Seat> bookedSeatsByUser = u.getBookedSeats();
            bookedSeatsByUser.removeAll(seatsToDelete);
            userService.saveUser(u);
        }

        seatsForShowRepostiory.delete(seatsForShow);
        seatRepository.deleteAll(seatsToDelete);
        showRepository.delete(showToDelete);
        return "redirect:/";
    }

    @GetMapping("/edit-movie/{id}")
    public String editMovie(@PathVariable Long id, Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        Movie movie = movieService.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/movies-list";
        }
        model.addAttribute("movie", movie);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "add-film";
    }

    @PostMapping("/delete-movie/{id}")
    @Transactional
    public String deleteMovie(@PathVariable Long id, Model model) {
        Movie movie = movieService.findById(id).orElse(null);
        if (movie == null) {
            return "redirect:/movies-list";
        }
        List<Show> showWithThatMovie = showRepository.findAllByMovie(movie);
        for (Show s : showWithThatMovie
        ) {
            SeatsForShow seatsForShow = s.getSeatsForShow();
            List<Seat> seatsToDelete = new ArrayList<>();
            seatsToDelete.addAll(seatsForShow.getGoldSeats());
            seatsToDelete.addAll(seatsForShow.getBronzeSeats());
            seatsToDelete.addAll(seatsForShow.getSilverSeats());
            List<User> users = userService.findAll();
            for (User u : users
            ) {
                List<Seat> bookedSeatsByUser = u.getBookedSeats();
                bookedSeatsByUser.removeAll(seatsToDelete);
                userService.saveUser(u);
            }
            seatsForShowRepostiory.delete(seatsForShow);
            seatRepository.deleteAll(seatsToDelete);
            showRepository.delete(s);
        }


        movieService.delete(movie);
        return "redirect:/movies-list";
    }

    @GetMapping("/screens")
    public String getScreens(Model model, Principal principal) {
        List<Screen> screenList = screenRepository.findAll();
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        model.addAttribute("screensList", screenList);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "all-screens";
    }

    @PostMapping("/add-screen")
    public String addScreen(@RequestParam String name, @RequestParam int goldSeats, @RequestParam int silverSeats,
                            @RequestParam int bronzeSeats, Model model) {
        if (goldSeats > 1 && name.length() > 3 && silverSeats > 1 && bronzeSeats > 1) {
            screenRepository.save(
                Screen.builder().name(name).goldSeats(goldSeats).silverSeats(silverSeats).bronzeSeats(bronzeSeats)
                    .build());
        }
        return "redirect:/screens";
    }

    @PostMapping("/delete-screen/{id}")
    public String deleteScreen(@PathVariable Long id, Model model) {
        screenRepository.findById(id).ifPresent(screenRepository::delete);
        return "redirect:/screens";
    }

    @GetMapping("/set-movie-show")
    public String addShowInput(Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        List<Screen> screenList = screenRepository.findAll();
        List<Movie> movieList = movieService.findAll();
        model.addAttribute("screensList", screenList);
        model.addAttribute("movieList", movieList);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "add-show";
    }

    @PostMapping("/add-show")
    @Transactional
    public String addShow(@RequestParam String date, @RequestParam String time, @RequestParam Long filmId,
                          @RequestParam Long screenId, Model model) {
        Movie movie = movieService.findById(filmId).orElse(null);
        Screen screen = screenRepository.findById(screenId).orElse(null);
        if (screen == null || movie == null) {
            return "redirect:/add-show";
        }

        List<Seat> goldSeats = new ArrayList<>();
        List<Seat> silverSeats = new ArrayList<>();
        List<Seat> bronzeSeats = new ArrayList<>();
        for (int i = 0; i < screen.getGoldSeats(); i++) {
            Seat seat = new Seat();
            seat.setId(null);
            seat.setBooked(false);
            goldSeats.add(seat);
        }
        for (int i = 0; i < screen.getSilverSeats(); i++) {
            Seat seat = new Seat();
            seat.setId(null);
            seat.setBooked(false);
            silverSeats.add(seat);
        }
        for (int i = 0; i < screen.getBronzeSeats(); i++) {
            Seat seat = new Seat();
            seat.setId(null);
            seat.setBooked(false);
            bronzeSeats.add(seat);
        }
        seatRepository.saveAll(goldSeats);
        seatRepository.saveAll(silverSeats);
        seatRepository.saveAll(bronzeSeats);

        SeatsForShow seatsForShow = new SeatsForShow();
        seatsForShow.setGoldSeats(goldSeats);
        seatsForShow.setSilverSeats(silverSeats);
        seatsForShow.setBronzeSeats(bronzeSeats);
        seatsForShowRepostiory.save(seatsForShow);

        Show newShow = Show.builder()
            .dateOfShow(LocalDate.parse(date))
            .timeOfShow(LocalTime.parse(time))
            .movie(movie)
            .screen(screen)
            .seatsForShow(seatsForShow)
            .build();
        showRepository.save(newShow);
        return "redirect:/screens";
    }

    @GetMapping("/book-show/{id}")
    public String getBookSeatPage(@PathVariable Long id, Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        Optional<Show> show = showRepository.findById(id);
        if (show.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("show", show.get());
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "booking-page";
    }

    @GetMapping("/book-seat/{id}")
    @Transactional
    public String bookSeat(@PathVariable Long id, Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        Seat seat = seatRepository.findById(id).orElse(null);
        if (seat == null) {
            return "redirect:/";
        }
        List<Show> allShows = showRepository.findAll();
        Show showWithThatSeat = null;
        for (Show s : allShows
        ) {
            if (s.getSeatsForShow().getGoldSeats().contains(seat) ||
                s.getSeatsForShow().getBronzeSeats().contains(seat) ||
                s.getSeatsForShow().getSilverSeats().contains(seat)) {
                showWithThatSeat = s;
            }
        }
        if (showWithThatSeat == null) {
            return "redirect:/";
        }
        if (user == null) {
            return "redirect:/book-show/" + showWithThatSeat.getId();
        }

        if (seat.isBooked() && user.getBookedSeats().contains(seat)) {
            user.removeBookedSeat(seat);
            seat.setBooked(false);
        } else if (seat.isBooked() && !user.getBookedSeats().contains(seat)) {
            return "redirect:/book-show/" + showWithThatSeat.getId();
        } else {
            user.addBookedSeat(seat);
            seat.setBooked(true);
        }


        userService.saveUser(user);
        seatRepository.save(seat);
        return "redirect:/book-show/" + showWithThatSeat.getId();
    }

    @GetMapping("/delete-booked-seat/{id}")
    @Transactional
    public String deleteBookedSeat(@PathVariable Long id, Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        Seat seat = seatRepository.findById(id).orElse(null);
        if (seat == null) {
            return "redirect:/";
        }
        User userWithThatSeat = userService.findBySeat(seat);
        seat.setBooked(false);
        userWithThatSeat.removeBookedSeat(seat);
        userService.saveUser(userFromRequest);
        seatRepository.save(seat);
        return "redirect:/all-booked-seats";
    }


    @GetMapping("/booked-seats")
    public String allBookedSeats(Model model, Principal principal) {
        User userFromRequest = userService.findByUsername(principal.getName()).orElse(null);
        if (userFromRequest == null) {
            return "redirect:/login";
        }

        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null) {
            return "redirect:/";
        }
        List<Seat> userSeats = user.getBookedSeats();
        List<UserSeatDTO> userSeatDTOS = new ArrayList<>();
        for (Seat s : userSeats
        ) {
            List<Show> allShows = showRepository.findAll();
            for (Show show : allShows
            ) {
                if (show.getSeatsForShow().getGoldSeats().contains(s) ||
                    show.getSeatsForShow().getBronzeSeats().contains(s) ||
                    show.getSeatsForShow().getSilverSeats().contains(s)) {
                    userSeatDTOS.add(
                        UserSeatDTO.builder()
                            .seatId(s.getId().toString())
                            .filmName(show.getMovie().getName())
                            .dateOfShow(show.getDateOfShow().toString())
                            .timeOfShow(show.getTimeOfShow().toString())
                            .build()
                    );
                }
            }
        }
        model.addAttribute("seats", userSeatDTOS);
        model.addAttribute("isManager", userFromRequest.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "my-seats";
    }

    @GetMapping("/all-booked-seats")
    public String bookSeat(Model model, Principal principal) {
        User user = userService.findByUsername(principal.getName()).orElse(null);
        if (user == null || !user.getRoles().contains(roleRepository.findByName("ROLE_MANAGER"))) {
            return "redirect:/";
        }
        List<UserSeatDTO> userSeatDTOS = new ArrayList<>();
        List<User> allUsers = userService.findAll();
        for (User u : allUsers
        ) {
            List<Seat> userSeats = u.getBookedSeats();
            for (Seat s : userSeats
            ) {
                List<Show> allShows = showRepository.findAll();
                for (Show show : allShows
                ) {
                    if (show.getSeatsForShow().getGoldSeats().contains(s) ||
                        show.getSeatsForShow().getBronzeSeats().contains(s) ||
                        show.getSeatsForShow().getSilverSeats().contains(s)) {
                        userSeatDTOS.add(
                            UserSeatDTO.builder()
                                .seatId(s.getId().toString())
                                .filmName(show.getMovie().getName())
                                .dateOfShow(show.getDateOfShow().toString())
                                .timeOfShow(show.getTimeOfShow().toString())
                                .userName(u.getUsername())
                                .build()
                        );
                    }
                }
            }
        }
        model.addAttribute("seats", userSeatDTOS);
        model.addAttribute("isManager", user.getRoles().contains(roleRepository.findByName("ROLE_MANAGER")));
        return "all-seats";
    }
}
