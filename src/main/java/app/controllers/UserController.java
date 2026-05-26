package app.controllers;

import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
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
        requireAuth(ctx);

        List<UserResponseDTO> response = userService.getAll();

        ctx.json(response);
    }

    public void getById(Context ctx)
    {
        requireAuth(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        UserResponseDTO response = userService.getById(id);

        ctx.json(response);
    }

    public void register(Context ctx)
    {
        System.out.println("regsiter reached");
        CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);
        System.out.println("request created");

        UserResponseDTO response = userService.register(request);
        System.out.println("response created");

        ctx.status(201).json(response);
    }

    public void create(Context ctx)
    {
        requireAuth(ctx);

        CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);

        UserResponseDTO response = userService.create(request);

        ctx.status(201).json(response);
    }

    public void update(Context ctx)
    {
        requireAuth(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));
        UpdateUserRequestDTO request = ctx.bodyAsClass(UpdateUserRequestDTO.class);

        UserResponseDTO response = userService.update(id, request);

        ctx.status(200).json(response);
    }

    public void delete(Context ctx)
    {
        requireAuth(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        userService.delete(id);

        ctx.status(204);
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