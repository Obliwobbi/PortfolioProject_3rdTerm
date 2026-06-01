package app.services;

import app.daos.CompanyDAO;
import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;
import app.entities.Company;
import app.entities.Role;
import app.exceptions.ConflictException;
import app.interfaces.ICompanyService;
import io.javalin.http.Context;

import java.util.List;

public class CompanyServiceImpl implements ICompanyService
{
    private final CompanyDAO companyDAO;


    public CompanyServiceImpl (CompanyDAO companyDAO)
    {
        this.companyDAO = companyDAO;
    }

    @Override
    public List<CompanyResponseDTO> getAll()
    {
        return companyDAO.getAll().stream()
                .map(company -> new CompanyResponseDTO(
                        company.getId(),
                        company.getName()
                ))
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
    public CompanyResponseDTO getById(Long id)
    {
        Company company = companyDAO.getById(id);

        return new CompanyResponseDTO(
                company.getId(),
                company.getName()
        );
    }

    @Override
    public CompanyResponseDTO create(CreateCompanyRequestDTO request)
    {
        if (companyDAO.findByName(request.name()).isPresent())
        {
            throw new ConflictException("Company already exists with name: " + request.name());
        }

        Company company = Company.builder()
                .name(request.name())
                .build();

        Company created = companyDAO.create(company);

        return new CompanyResponseDTO(
                created.getId(),
                created.getName()
        );
    }

    @Override
    public CompanyResponseDTO update(Long id, UpdateCompanyRequestDTO request)
    {
        Company company = companyDAO.getById(id);
        company.setName(request.name());

        Company updated = companyDAO.update(company);

        return new CompanyResponseDTO(
                updated.getId(),
                updated.getName()
        );
    }

    @Override
    public boolean delete(Long id)
    {
        Company company = companyDAO.getById(id);
        if(company == null)
        {
            return false;
        }
        companyDAO.delete(company);
        return true;
    }

    private CompanyResponseDTO mapToResponseDTO(Company company)
    {
        return new CompanyResponseDTO(
                company.getId(),
                company.getName()
        );
    }

}
