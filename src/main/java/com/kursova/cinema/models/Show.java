package com.kursova.cinema.models;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "shows")
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class Show {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate dateOfShow;
    private LocalTime timeOfShow;
    @ManyToOne(fetch = FetchType.EAGER)
    private Screen screen;
    @ManyToOne(fetch = FetchType.EAGER)
    private Movie movie;
    @OneToOne(fetch = FetchType.EAGER)
    private SeatsForShow seatsForShow;

}
