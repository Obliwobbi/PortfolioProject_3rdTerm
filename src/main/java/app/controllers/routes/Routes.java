package app.controllers.routes;

import app.controllers.AuthController;
import app.controllers.CompanyController;
import app.controllers.UserController;
import io.javalin.apibuilder.EndpointGroup;

import java.util.Map;

import static io.javalin.apibuilder.ApiBuilder.*;

public class Routes
{
    private static final String API_VERSION = "api/v1";

    private final AuthRoutes authRoutes;
    private final UserRoutes userRoutes;
    private final CompanyRoutes companyRoutes;

    public Routes(AuthController authController, UserController userController, CompanyController companyController)
    {
        this.authRoutes = new AuthRoutes(authController);
        this.userRoutes = new UserRoutes(userController);
        this.companyRoutes = new CompanyRoutes(companyController);
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            get("/", ctx -> ctx.status(200).json(Map.of("message", "MemberSystem API is running")));

            path(API_VERSION, () ->
            {
                authRoutes.getRoutes().addEndpoints();
                userRoutes.getRoutes().addEndpoints();
                companyRoutes.getRoutes().addEndpoints();
            });
        };
    }

    public static String getApiVersion()
    {
        return API_VERSION;
    }
}