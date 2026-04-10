package app.dto.randomuser;

import java.util.List;

public record RandomUserResponseDTO(
        List<RandomUserResultDTO> results
)
{
}
