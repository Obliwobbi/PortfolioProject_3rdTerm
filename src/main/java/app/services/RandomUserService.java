package app.services;

import app.dto.randomuser.RandomUserViewDTO;
import app.dto.randomuser.RandomUserResponseDTO;
import app.dto.randomuser.RandomUserResultDTO;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class RandomUserService {

    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;

    public RandomUserService() {
        this.httpClient = HttpClient.newHttpClient();
        this.objectMapper = new ObjectMapper()
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public List<RandomUserViewDTO> fetchRandomUsers(int count) {
        try {
            String url = "https://randomuser.me/api/?results=" + count + "&nat=dk";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .GET()
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new IllegalStateException("RandomUser API returned status " + response.statusCode());
            }

            RandomUserResponseDTO randomUserResponse = objectMapper.readValue(
                    response.body(),
                    RandomUserResponseDTO.class
            );

            return randomUserResponse.results().stream()
                    .map(this::mapToViewDTO)
                    .toList();

        } catch (IOException | InterruptedException exception) {
            throw new RuntimeException("Failed to fetch random users", exception);
        }
    }

    private RandomUserViewDTO mapToViewDTO(RandomUserResultDTO result) {
        String dob = result.dob() != null ? result.dob().date() : null;

        return new RandomUserViewDTO(
                result.email(),
                result.name() != null ? result.name().first() : null,
                result.name() != null ? result.name().last() : null,
                dob
        );
    }
}