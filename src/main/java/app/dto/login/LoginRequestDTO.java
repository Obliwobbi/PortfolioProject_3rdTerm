package app.dto.login;

public record LoginRequestDTO(
        String email,
        String password
) {}