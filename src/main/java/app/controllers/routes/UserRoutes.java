package app.controllers.routes;

import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class UserRoutes
{
    private final UserController userController;

    public UserRoutes(UserController userController)
    {
        this.userController = userController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("users", () ->
            {
                get(userController::getAll);
                post(userController::create);

                path("{id}", () ->
                {
                    get(userController::getById);
                    put(userController::update);
                    delete(userController::delete);
                });
            });
        };
    }
}