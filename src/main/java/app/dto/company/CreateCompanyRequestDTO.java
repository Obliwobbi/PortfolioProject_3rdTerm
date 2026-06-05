package app.dto.company;

public record CreateCompanyRequestDTO(
        String name,
        Boolean publicRegistrationEnabled
) {}
