package app.controllers;

import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;
import app.exceptions.UnauthorizedException;
import app.interfaces.ICompanyService;
import app.services.JwtService;
import io.javalin.http.Context;
import jakarta.persistence.EntityNotFoundException;

import java.util.List;

public class CompanyController
{
    private final ICompanyService companyService;
    private final JwtService jwtService;

    public CompanyController (ICompanyService companyService, JwtService jwtService)
    {
        this.companyService = companyService;
        this.jwtService = jwtService;
    }

    public void getPublicCompanies(Context ctx)
    {
        List<CompanyResponseDTO> response = companyService.getAll();

        ctx.json(response);
    }

    public void getAll(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        List<CompanyResponseDTO> response = companyService.getAllVisibleTo(authUser);

        ctx.json(response);
    }

    public void getById(Context ctx)
    {
        Long id = Long.parseLong(ctx.pathParam("id"));

        CompanyResponseDTO response = companyService.getById(id);

        ctx.json(response);
    }

    public void create(Context ctx)
    {
        requireAuth(ctx);

        CreateCompanyRequestDTO request = ctx.bodyAsClass(CreateCompanyRequestDTO.class);

        CompanyResponseDTO response = companyService.create(request);

        ctx.status(201).json(response);
    }

    public void update(Context ctx)
    {
        requireAuth(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));
        UpdateCompanyRequestDTO request = ctx.bodyAsClass(UpdateCompanyRequestDTO.class);

        CompanyResponseDTO response = companyService.update(id, request);

        ctx.status(200).json(response);
    }

    public void delete(Context ctx)
    {
        requireAuth(ctx);
        Long id = Long.parseLong(ctx.pathParam("id"));
        boolean deleteCheck = companyService.delete(id);
        if(!deleteCheck)
        {
            throw new EntityNotFoundException("Company doesnt exist on id: " + id);
        }
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
