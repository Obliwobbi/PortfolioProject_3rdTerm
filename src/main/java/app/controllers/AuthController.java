package app.controllers;

import app.daos.UserDAO;
import app.dto.login.LoginRequestDTO;
import app.dto.login.LoginResponseDTO;
import app.dto.user.UserResponseDTO;
import app.entities.User;
import app.services.AuthService;
import app.services.JwtService;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

public class AuthController
{
    private final AuthService authService;
    private final JwtService jwtService;
    private final UserDAO userDAO;

    public AuthController(AuthService authService, JwtService jwtService, UserDAO userDAO)
    {
        this.authService = authService;
        this.jwtService = jwtService;
        this.userDAO = userDAO;
    }

    public void login(Context ctx)
    {
        LoginRequestDTO request = ctx.bodyAsClass(LoginRequestDTO.class);

        String token = authService.login(request.email(), request.password());

        ctx.status(200).json(new LoginResponseDTO(token));
    }

    public void me(Context ctx)
    {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new app.exceptions.UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length());

        jwtService.verifyToken(token);

        String email = jwtService.getEmailFromToken(token);

        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("User not found with email: " + email));

        User userWithCompany = userDAO.getByIdWithCompany(user.getId());

        UserResponseDTO response = new UserResponseDTO(
                userWithCompany.getId(),
                userWithCompany.getEmail(),
                userWithCompany.getFirstname(),
                userWithCompany.getLastname(),
                userWithCompany.getDob(),
                userWithCompany.getRole(),
                userWithCompany.getCompany().getId(),
                userWithCompany.getCompany().getName()
        );

        ctx.json(response);
    }
}