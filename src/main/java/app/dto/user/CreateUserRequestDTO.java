package app.dto.user;

import app.entities.Role;

import java.time.LocalDate;

public record CreateUserRequestDTO(
        Long companyId,
        String email,
        String firstname,
        String lastname,
        LocalDate dob,
        Role role,
        String passwordHash
) {}
