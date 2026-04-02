package app.dto.user;

import app.entities.Role;

import java.time.LocalDate;

public record UpdateUserRequestDTO(
        String firstname,
        String lastname,
        LocalDate dob,
        Role role
) {}
