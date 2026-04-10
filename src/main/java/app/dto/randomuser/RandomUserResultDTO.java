package app.dto.randomuser;

public record RandomUserResultDTO(
        String email,
        RandomUserNameDTO name,
        RandomUserDobDTO dob
)
{
}
