package app.controllers;

import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;
import app.exceptions.UnauthorizedException;
import app.interfaces.ICompanyService;
import app.services.JwtService;
import io.javalin.http.Context;

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
        List<CompanyResponseDTO> response = companyService.getPublicCompanies();

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
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        CompanyResponseDTO response = companyService.getByIdVisibleTo(id, authUser);

        ctx.json(response);
    }

    public void create(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        CreateCompanyRequestDTO request = ctx.bodyAsClass(CreateCompanyRequestDTO.class);

        CompanyResponseDTO response = companyService.createVisibleTo(request, authUser);

        ctx.status(201).json(response);
    }

    public void update(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));
        UpdateCompanyRequestDTO request = ctx.bodyAsClass(UpdateCompanyRequestDTO.class);

        CompanyResponseDTO response = companyService.updateVisibleTo(id, request, authUser);

        ctx.status(200).json(response);
    }

    public void delete(Context ctx)
    {
        AuthUserDTO authUser = getAuthUser(ctx);

        Long id = Long.parseLong(ctx.pathParam("id"));

        companyService.deleteVisibleTo(id, authUser);

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

}
