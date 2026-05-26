package app.controllers.routes;

import app.controllers.AuthController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.get;
import static io.javalin.apibuilder.ApiBuilder.post;

public class AuthRoutes
{
    private final AuthController authController;

    public AuthRoutes(AuthController authController)
    {
        this.authController = authController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            post("login", authController::login);
            get("me", authController::me);
        };
    }
}