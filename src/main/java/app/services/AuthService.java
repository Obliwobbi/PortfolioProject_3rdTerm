package app.services;

import app.daos.UserDAO;
import app.dto.login.LoginUserDTO;
import app.entities.User;
import app.exceptions.UnauthorizedException;

public class AuthService
{

    private final UserDAO userDAO;
    private final PasswordService passwordService;
    private final JwtService jwtService;

    public AuthService(UserDAO userDAO, PasswordService passwordService, JwtService jwtService)
    {
        this.userDAO = userDAO;
        this.passwordService = passwordService;
        this.jwtService = jwtService;
    }

    public String login(String email, String password)
    {
        User user = userDAO.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        boolean validPassword = passwordService.verifyPassword(password, user.getPasswordHash());

        if (!validPassword)
        {
            throw new UnauthorizedException("Invalid email or password");
        }

        LoginUserDTO loginUserDTO = new LoginUserDTO(
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getCompany().getId()
        );

        return jwtService.createToken(loginUserDTO);
    }


}