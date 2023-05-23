package com.kursova.cinema.DTOs;

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
public class UserSeatDTO {
    private String filmName;
    private String dateOfShow;
    private String timeOfShow;
    private String seatId;
    private String userName;
}
