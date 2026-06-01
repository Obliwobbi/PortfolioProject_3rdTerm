package app.dto.login;

import app.entities.Role;

public record AuthUserDTO(
        Long userId,
        String email,
        Role role,
        Long companyId
)
{
}
