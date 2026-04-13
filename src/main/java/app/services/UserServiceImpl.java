package app.services;

import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
import app.entities.Company;
import app.entities.User;
import app.interfaces.IUserService;

import java.util.List;

public class UserServiceImpl implements IUserService
{
    private final UserDAO userDAO;
    private final CompanyDAO companyDAO;
    private final PasswordService passwordService;

    public UserServiceImpl(UserDAO userDAO, CompanyDAO companyDAO, PasswordService passwordService)
    {
        this.userDAO = userDAO;
        this.companyDAO = companyDAO;
        this.passwordService = passwordService;
    }

    @Override
    public UserResponseDTO create(CreateUserRequestDTO request)
    {
        Company company = companyDAO.getById(request.companyId());

        String hashedPassword = passwordService.hashPassword(request.password());

        User user = User.builder()
                .company(company)
                .email(request.email())
                .firstname(request.firstname())
                .lastname(request.lastname())
                .dob(request.dob())
                .role(request.role())
                .passwordHash(hashedPassword)
                .build();

        User created = userDAO.create(user);
        User createdWithCompany = userDAO.getByIdWithCompany(created.getId());

        return mapToResponseDTO(createdWithCompany);
    }

    @Override
    public UserResponseDTO getById(Long id)
    {
        return null;
    }

    @Override
    public List<UserResponseDTO> getAll()
    {
        return List.of();
    }

    @Override
    public UserResponseDTO update(Long id, UpdateUserRequestDTO request)
    {
        return null;
    }

    @Override
    public void delete(Long id)
    {

    }

    private UserResponseDTO mapToResponseDTO(User user)
    {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFirstname(),
                user.getLastname(),
                user.getDob(),
                user.getRole(),
                user.getCompany().getId(),
                user.getCompany().getName()
        );
    }
}
