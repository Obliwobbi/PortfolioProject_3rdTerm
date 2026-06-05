package app.dto.company;

public record CompanyResponseDTO(
        Long id,
        String name,
        Boolean publicRegistrationEnabled
) {}
