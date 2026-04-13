package app;

import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.LoginResponseDTO;
import app.dto.randomuser.RandomUserViewDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
import app.entities.Company;
import app.entities.User;
import app.exceptions.ApiErrorResponse;
import app.exceptions.UnauthorizedException;
import app.services.*;

import io.javalin.Javalin;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class ApplicationConfig
{

    public static Javalin startApp(int port, EntityManagerFactory emf)
    {
        CompanyDAO companyDAO = new CompanyDAO(emf);
        UserDAO userDAO = new UserDAO(emf);
        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService();
        AuthService authService = new AuthService(userDAO, passwordService, jwtService);
        UserServiceImpl userService = new UserServiceImpl(userDAO, companyDAO, passwordService);
        RandomUserService randomUserService = new RandomUserService();

        Javalin app = Javalin.create(config ->
        {
            config.router.apiBuilder(() ->
            {
                // TODO: Split routes into separate controller classes later.
            });
        });

        app.before(ctx -> System.out.println("Incoming request: " + ctx.method() + " " + ctx.path()));
        app.after(ctx -> System.out.println("Response status: " + ctx.status()));

        // --------------------
        // Exception handlers
        // --------------------

        app.exception(EntityNotFoundException.class, (e, ctx) ->
        {
            ctx.status(404);
            ctx.json(new ApiErrorResponse(404, e.getMessage()));
        });

        app.exception(IllegalArgumentException.class, (e, ctx) ->
        {
            ctx.status(400);
            ctx.json(new ApiErrorResponse(400, e.getMessage()));
        });

        app.exception(UnauthorizedException.class, (e, ctx) -> {
            ctx.status(401);
            ctx.json(new app.exceptions.ApiErrorResponse(401, e.getMessage()));
        });

        app.exception(Exception.class, (e, ctx) ->
        {
            e.printStackTrace();
            ctx.status(500);
            ctx.json(new ApiErrorResponse(500, "Internal server error"));
        });

        app.get("/", ctx -> ctx.result("MemberSystem API is running"));

        // --------------------
        // Authentication endpoints
        // --------------------
        // TODO: Add authentication endpoints like /login.

        app.post("/login", ctx -> {
            var request = ctx.bodyAsClass(app.dto.login.LoginRequestDTO.class);

            String token = authService.login(request.email(), request.password());

            ctx.status(200);
            ctx.json(new LoginResponseDTO(token));
        });

        // --------------------
        // Company endpoints
        // --------------------

        app.get("/companies", ctx ->
        {
            List<CompanyResponseDTO> response = companyDAO.getAll().stream()
                    .map(company -> new CompanyResponseDTO(
                            company.getId(),
                            company.getName()
                    ))
                    .toList();

            ctx.json(response);
        });

        app.get("/companies/{id}", ctx ->
        {
            Long id = Long.parseLong(ctx.pathParam("id"));
            Company company = companyDAO.getById(id);

            CompanyResponseDTO response = new CompanyResponseDTO(
                    company.getId(),
                    company.getName()
            );

            ctx.json(response);
        });

        app.post("/companies", ctx ->
        {
            requireAuth(ctx, jwtService);

            CreateCompanyRequestDTO request = ctx.bodyAsClass(CreateCompanyRequestDTO.class);

            Company company = Company.builder()
                    .name(request.name())
                    .build();

            Company created = companyDAO.create(company);

            CompanyResponseDTO response = new CompanyResponseDTO(
                    created.getId(),
                    created.getName()
            );

            ctx.status(201);
            ctx.json(response);
        });

        app.put("/companies/{id}", ctx ->
        {
            requireAuth(ctx, jwtService);

            Long id = Long.parseLong(ctx.pathParam("id"));
            UpdateCompanyRequestDTO request = ctx.bodyAsClass(UpdateCompanyRequestDTO.class);

            Company company = companyDAO.getById(id);
            company.setName(request.name());

            Company updated = companyDAO.update(company);

            CompanyResponseDTO response = new CompanyResponseDTO(
                    updated.getId(),
                    updated.getName()
            );

            ctx.json(response);
        });

        app.delete("/companies/{id}", ctx ->
        {
            requireAuth(ctx, jwtService);

            Long id = Long.parseLong(ctx.pathParam("id"));
            Company company = companyDAO.getById(id);

            companyDAO.delete(company);
            ctx.status(204);
        });

        // --------------------
        // User endpoints
        // --------------------

        app.get("/users", ctx ->
        {
            requireAuth(ctx, jwtService);

            List<UserResponseDTO> response = userDAO.getAllWithCompany().stream()
                    .map(user -> new UserResponseDTO(
                            user.getId(),
                            user.getEmail(),
                            user.getFirstname(),
                            user.getLastname(),
                            user.getDob(),
                            user.getRole(),
                            user.getCompany().getId(),
                            user.getCompany().getName()
                    ))
                    .toList();

            ctx.json(response);
        });

        app.get("/users/{id}", ctx ->
        {
            requireAuth(ctx, jwtService);

            Long id = Long.parseLong(ctx.pathParam("id"));
            User user = userDAO.getByIdWithCompany(id);

            UserResponseDTO response = new UserResponseDTO(
                    user.getId(),
                    user.getEmail(),
                    user.getFirstname(),
                    user.getLastname(),
                    user.getDob(),
                    user.getRole(),
                    user.getCompany().getId(),
                    user.getCompany().getName()
            );

            ctx.json(response);
        });

        app.post("/users", ctx ->
        {
            requireAuth(ctx, jwtService);

            CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);
            UserResponseDTO response = userService.create(request);

            ctx.status(201);
            ctx.json(response);
        });

        app.put("/users/{id}", ctx ->
        {
            requireAuth(ctx, jwtService);

            Long id = Long.parseLong(ctx.pathParam("id"));
            UpdateUserRequestDTO request = ctx.bodyAsClass(UpdateUserRequestDTO.class);

            User user = userDAO.getByIdWithCompany(id);

            user.setFirstname(request.firstname());
            user.setLastname(request.lastname());
            user.setDob(request.dob());
            user.setRole(request.role());

            User updated = userDAO.update(user);
            User updatedWithCompany = userDAO.getByIdWithCompany(updated.getId());

            UserResponseDTO response = new UserResponseDTO(
                    updatedWithCompany.getId(),
                    updatedWithCompany.getEmail(),
                    updatedWithCompany.getFirstname(),
                    updatedWithCompany.getLastname(),
                    updatedWithCompany.getDob(),
                    updatedWithCompany.getRole(),
                    updatedWithCompany.getCompany().getId(),
                    updatedWithCompany.getCompany().getName()
            );

            ctx.json(response);
        });

        app.delete("/users/{id}", ctx ->
        {
            requireAuth(ctx, jwtService);

            Long id = Long.parseLong(ctx.pathParam("id"));
            User user = userDAO.getById(id);

            userDAO.delete(user);
            ctx.status(204);
        });

        // --------------------
        // RandomUser endpoints
        // --------------------

        app.get("/randomusers", ctx ->
        {
            List<RandomUserViewDTO> randomUsers =
                    randomUserService.fetchRandomUsers(10);

            ctx.json(randomUsers);
        });

        app.get("/randomusers/{count}", ctx ->
        {
            Integer randomUserCount = Integer.parseInt(ctx.pathParam("count"));
            List<RandomUserViewDTO> randomUsers =
                    randomUserService.fetchRandomUsers(randomUserCount);

            ctx.json(randomUsers);
        });

        // --------------------
        // Membership endpoints
        // --------------------
        // TODO: Add routes for Membership.

        // --------------------
        // Location endpoints
        // --------------------
        // TODO: Add routes for Location.

        // --------------------
        // CheckIn endpoints
        // --------------------
        // TODO: Add routes for CheckIn.

        // TODO: Move route handlers into controller classes.

        app.start(port);
        return app;
    }

    // --------------------
    // Helper methods
    // --------------------

    private static void requireAuth(io.javalin.http.Context ctx, JwtService jwtService) {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length());
        jwtService.verifyToken(token);
    }
}