package app.interfaces;

import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;

import java.util.List;

public interface ICompanyService
{
    List<CompanyResponseDTO> getPublicCompanies();

    List<CompanyResponseDTO> getAllVisibleTo(AuthUserDTO authUser);

    CompanyResponseDTO getByIdVisibleTo(Long id, AuthUserDTO authUser);

    CompanyResponseDTO create(CreateCompanyRequestDTO request);

    CompanyResponseDTO createVisibleTo(CreateCompanyRequestDTO request, AuthUserDTO authUser);

    CompanyResponseDTO updateVisibleTo(Long id, UpdateCompanyRequestDTO request, AuthUserDTO authUser);

    void deleteVisibleTo(Long id, AuthUserDTO authUser);
}
