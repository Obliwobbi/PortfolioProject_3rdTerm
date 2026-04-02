package app.exceptions;

public record ApiErrorResponse(
        int status,
        String message
) {}
