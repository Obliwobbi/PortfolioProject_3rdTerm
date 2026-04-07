package app.dto.login;

public record LoginUserDTO(
        Long id,
        String email,
        String role,
        Long companyId
) {}