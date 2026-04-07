package app;

import app.config.HibernateConfig;
import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
import app.entities.Company;
import app.entities.User;
import app.exceptions.ApiErrorResponse;
import app.exceptions.UnauthorizedException;
import app.services.AuthService;
import app.services.JwtService;
import app.services.PasswordService;
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
            CreateUserRequestDTO request = ctx.bodyAsClass(CreateUserRequestDTO.class);

            Company company = companyDAO.getById(request.companyId());

            User user = User.builder()
                    .company(company)
                    .email(request.email())
                    .firstname(request.firstname())
                    .lastname(request.lastname())
                    .dob(request.dob())
                    .role(request.role())
                    .passwordHash(request.passwordHash())
                    .build();

            User created = userDAO.create(user);
            User createdWithCompany = userDAO.getByIdWithCompany(created.getId());

            UserResponseDTO response = new UserResponseDTO(
                    createdWithCompany.getId(),
                    createdWithCompany.getEmail(),
                    createdWithCompany.getFirstname(),
                    createdWithCompany.getLastname(),
                    createdWithCompany.getDob(),
                    createdWithCompany.getRole(),
                    createdWithCompany.getCompany().getId(),
                    createdWithCompany.getCompany().getName()
            );

            ctx.status(201);
            ctx.json(response);
        });

        app.put("/users/{id}", ctx ->
        {
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
            Long id = Long.parseLong(ctx.pathParam("id"));
            User user = userDAO.getById(id);

            userDAO.delete(user);
            ctx.status(204);
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


        // TODO: Add authentication endpoints like /login.
        // TODO: Move route handlers into controller classes.

        app.start(port);
        return app;
    }

    public static void main(String[] args)
    {
        startApp(7000, HibernateConfig.getEntityManagerFactory());
    }
}