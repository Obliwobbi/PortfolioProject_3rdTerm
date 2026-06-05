package app.dto.company;

public record UpdateCompanyRequestDTO(
        String name,
        Boolean publicRegistrationEnabled
) {}
