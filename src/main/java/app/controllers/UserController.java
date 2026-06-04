package app.controllers;

import app.dto.login.AuthUserDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
import app.exceptions.UnauthorizedException;
import app.interfaces.IUserService;
import app.services.JwtService;
import io.javalin.http.Context;

import java.util.List;

public class UserController
{
    private final IUserService userService;
    private final JwtService jwtService;

    public UserController(IUserService userService, JwtService jwtService)
    {
        this.userService = userService;
        this.jwtService = jwtService;
    }

    public void getAll(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        List<UserResponseDTO> response = userService.getAllVisibleTo(authUser);

        ctx.json(response);
    }

    public void getById(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        UserResponseDTO response = userService.getByIdVisibleTo(id, authUser);

        ctx.json(response);
    }

    public void register(Context ctx)
    {
        CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);

        UserResponseDTO response = userService.register(request);


        ctx.status(201).json(response);
    }

    public void create(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);

        UserResponseDTO response = userService.createVisibleTo(request, authUser);

        ctx.status(201).json(response);
    }

    public void update(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        UpdateUserRequestDTO request = ctx.bodyAsClass(UpdateUserRequestDTO.class);

        UserResponseDTO response = userService.updateVisibleTo(id, request, authUser);

        ctx.status(200).json(response);
    }

    public void delete(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        userService.deleteVisibleTo(id, authUser);

        ctx.status(204);
    }

    private AuthUserDTO getAuthUser(Context ctx)
    {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length());

        return jwtService.getAuthUserFromToken(token);
    }

    private void requireAuth(Context ctx)
    {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new app.exceptions.UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length());
        jwtService.verifyToken(token);
    }
}