package app.services;

import app.daos.CompanyDAO;
import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;
import app.entities.Company;
import app.entities.Role;
import app.exceptions.ConflictException;
import app.exceptions.ForbiddenException;
import app.interfaces.ICompanyService;

import java.util.List;

public class CompanyServiceImpl implements ICompanyService
{
    private final CompanyDAO companyDAO;


    public CompanyServiceImpl(CompanyDAO companyDAO)
    {
        this.companyDAO = companyDAO;
    }

    @Override
    public List<CompanyResponseDTO> getPublicCompanies()
    {
        return companyDAO.getAllPublicRegistrationEnabled().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<CompanyResponseDTO> getAllVisibleTo(AuthUserDTO authUser)
    {
        if (authUser.role() == Role.SYSTEM_ADMIN)
        {
            return companyDAO.getAll().stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }

        Company company = companyDAO.getById(authUser.companyId());

        return List.of(mapToResponseDTO(company));
    }

    @Override
    public CompanyResponseDTO getByIdVisibleTo(Long id, AuthUserDTO authUser)
    {
        Company company = companyDAO.getById(id);

        if (authUser.role() == Role.SYSTEM_ADMIN)
        {
            return mapToResponseDTO(company);
        }

        if (!authUser.companyId().equals(company.getId()))
        {
            throw new ForbiddenException("You can only view your own company");
        }

        return mapToResponseDTO(company);
    }

    @Override
    public CompanyResponseDTO create(CreateCompanyRequestDTO request)
    {
        if (companyDAO.findByName(request.name()).isPresent())
        {
            throw new ConflictException("Company already exists with name: " + request.name());
        }

        boolean publicRegistrationEnabled =
                request.publicRegistrationEnabled() != null
                        ? request.publicRegistrationEnabled()
                        : true;

        Company company = Company.builder()
                .name(request.name())
                .publicRegistrationEnabled(publicRegistrationEnabled)
                .build();

        Company created = companyDAO.create(company);

        return mapToResponseDTO(created);
    }

    @Override
    public CompanyResponseDTO createVisibleTo(CreateCompanyRequestDTO request, AuthUserDTO authUser)
    {
        if (authUser.role() != Role.SYSTEM_ADMIN)
        {
            throw new ForbiddenException("Only system admins can create companies");
        }

        return create(request);
    }

    @Override
    public CompanyResponseDTO updateVisibleTo(Long id, UpdateCompanyRequestDTO request, AuthUserDTO authUser)
    {
        if (authUser.role() == Role.MEMBER)
        {
            throw new ForbiddenException("Members are not allowed to update companies");
        }

        if (authUser.role() == Role.COMPANY_ADMIN && !authUser.companyId().equals(id))
        {
            throw new ForbiddenException("Company admins can only update their own company");
        }

        Company company = companyDAO.getById(id);
        company.setName(request.name());
        if (request.publicRegistrationEnabled() != null)
        {
            company.setPublicRegistrationEnabled(request.publicRegistrationEnabled());
        }

        Company updated = companyDAO.update(company);

        return mapToResponseDTO(updated);
    }

    @Override
    public void deleteVisibleTo(Long id, AuthUserDTO authUser)
    {
        if (authUser.role() != Role.SYSTEM_ADMIN)
        {
            throw new ForbiddenException("Only system admins can delete companies");
        }

        Company company = companyDAO.getById(id);

        companyDAO.delete(company);
    }

    private CompanyResponseDTO mapToResponseDTO(Company company)
    {
        return new CompanyResponseDTO(
                company.getId(),
                company.getName(),
                company.isPublicRegistrationEnabled()
        );
    }

}
