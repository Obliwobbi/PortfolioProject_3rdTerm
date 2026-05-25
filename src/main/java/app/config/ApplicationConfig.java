package app.config;

import app.controllers.AuthController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.randomuser.RandomUserViewDTO;
import app.entities.Company;
import app.exceptions.ApiErrorResponse;
import app.exceptions.ConflictException;
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

        AuthController authController = new AuthController(authService, jwtService, userDAO);
        UserController userController = new UserController(userService, jwtService);

        Routes routes = new Routes(authController, userController);

        Javalin app = Javalin.create(config ->
        {
            config.bundledPlugins.enableCors(cors ->
            {
                cors.addRule(it ->
                {
                    it.allowHost(
                            "https://membersystem.obli.dk",
                            "http://localhost:5173"
                    );
                });
            });

            config.router.apiBuilder(routes.getRoutes());
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

        app.exception(UnauthorizedException.class, (e, ctx) ->
        {
            ctx.status(401);
            ctx.json(new app.exceptions.ApiErrorResponse(401, e.getMessage()));
        });

        app.exception(ConflictException.class, (e, ctx) ->
        {
            ctx.status(409);
            ctx.json(new ApiErrorResponse(409, e.getMessage()));
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

            if (companyDAO.findByName(request.name()).isPresent())
            {
                throw new ConflictException("Company already exists with name: " + request.name());
            }

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

    private static void requireAuth(io.javalin.http.Context ctx, JwtService jwtService)
    {
        String authHeader = ctx.header("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer "))
        {
            throw new UnauthorizedException("Missing or invalid Authorization header");
        }

        String token = authHeader.substring("Bearer ".length());
        jwtService.verifyToken(token);
    }
}