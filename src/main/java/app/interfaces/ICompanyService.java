package app.interfaces;

import app.dto.company.CompanyResponseDTO;
import app.dto.company.CreateCompanyRequestDTO;
import app.dto.company.UpdateCompanyRequestDTO;
import app.dto.login.AuthUserDTO;

import java.util.List;

public interface ICompanyService
{
    List<CompanyResponseDTO> getAll();

    List<CompanyResponseDTO> getAllVisibleTo(AuthUserDTO authUser);

    CompanyResponseDTO getById(Long id);

    CompanyResponseDTO create(CreateCompanyRequestDTO request);

    CompanyResponseDTO update(Long id, UpdateCompanyRequestDTO updateCompanyRequestDTO);

    boolean delete(Long id);
}
