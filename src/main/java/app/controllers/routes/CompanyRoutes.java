package app.controllers.routes;

import app.controllers.CompanyController;
import io.javalin.apibuilder.EndpointGroup;

import static io.javalin.apibuilder.ApiBuilder.*;

public class CompanyRoutes
{
    private final CompanyController companyController;

    public CompanyRoutes (CompanyController companyController)
    {
        this.companyController = companyController;
    }

    public EndpointGroup getRoutes()
    {
        return () ->
        {
            path("companies", () ->
            {
                get(companyController::getAll);
                post(companyController::create);

                path("{id}", () ->
                {
                    get(companyController::getById);
                    put(companyController::update);
                    delete(companyController::delete);
                });
            });
        };
    }
}
