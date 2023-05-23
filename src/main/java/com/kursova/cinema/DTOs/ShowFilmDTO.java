package com.kursova.cinema.DTOs;

import com.kursova.cinema.models.Movie;
import com.kursova.cinema.models.Show;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class ShowFilmDTO {
    private Movie movie;
    private List<Show> showList = new ArrayList<>();

    public void addShow(Show show) {
        this.showList.add(show);
    }
}
