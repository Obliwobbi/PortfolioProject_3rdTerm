package app.dto.user;

import app.entities.Role;

import java.time.LocalDate;

public record UserResponseDTO(
        Long id,
        String email,
        String firstname,
        String lastname,
        LocalDate dob,
        Role role,
        Long companyId,
        String companyName
) {}
