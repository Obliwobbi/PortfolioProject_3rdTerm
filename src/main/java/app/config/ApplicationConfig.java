package app.config;

import app.controllers.AuthController;
import app.controllers.CompanyController;
import app.controllers.UserController;
import app.controllers.routes.Routes;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.randomuser.RandomUserViewDTO;
import app.exceptions.ApiErrorResponse;
import app.exceptions.ConflictException;
import app.exceptions.ForbiddenException;
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
        //DAO's
        CompanyDAO companyDAO = new CompanyDAO(emf);
        UserDAO userDAO = new UserDAO(emf);

        //Services
        PasswordService passwordService = new PasswordService();
        JwtService jwtService = new JwtService();

        AuthService authService = new AuthService(userDAO, passwordService, jwtService);
        UserServiceImpl userService = new UserServiceImpl(userDAO, companyDAO, passwordService);
        CompanyServiceImpl companyService = new CompanyServiceImpl(companyDAO);

        RandomUserService randomUserService = new RandomUserService();

        //Controllers
        AuthController authController = new AuthController(authService, jwtService, userDAO);
        UserController userController = new UserController(userService, jwtService);
        CompanyController companyController = new CompanyController(companyService, jwtService);

        Routes routes = new Routes(authController, userController, companyController);

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


        app.exception(IllegalArgumentException.class, (e, ctx) ->
        {
            ctx.status(400);
            ctx.json(new ApiErrorResponse(400, e.getMessage()));
        });

        app.exception(UnauthorizedException.class, (e, ctx) ->
        {
            ctx.status(401);
            ctx.json(new ApiErrorResponse(401, e.getMessage()));
        });

        app.exception(ForbiddenException.class, (e, ctx) -> {
            ctx.status(403);
            ctx.json(new ApiErrorResponse(403, e.getMessage()));
        });

        app.exception(EntityNotFoundException.class, (e, ctx) ->
        {
            ctx.status(404);
            ctx.json(new ApiErrorResponse(404, e.getMessage()));
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

        app.start(port);
        return app;
    }
}