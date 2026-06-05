package app.services;

import app.daos.CompanyDAO;
import app.daos.UserDAO;
import app.dto.login.AuthUserDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;
import app.entities.Company;
import app.entities.Role;
import app.entities.User;
import app.exceptions.ConflictException;
import app.exceptions.ForbiddenException;
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
    public List<UserResponseDTO> getAll()
    {
        return userDAO.getAllWithCompany().stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    @Override
    public List<UserResponseDTO> getAllVisibleTo(AuthUserDTO authUser)
    {
        if (authUser.role() == Role.SYSTEM_ADMIN)
        {
            return userDAO.getAllWithCompany().stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }

        if (authUser.role() == Role.COMPANY_ADMIN)
        {
            return userDAO.findByCompanyIdWithCompany(authUser.companyId()).stream()
                    .map(this::mapToResponseDTO)
                    .toList();
        }

        User user = userDAO.getByIdWithCompany(authUser.userId());

        return List.of(mapToResponseDTO(user));
    }

    @Override
    public UserResponseDTO create(CreateUserRequestDTO request)
    {
        if (userDAO.findByEmail(request.email()).isPresent())
        {
            throw new ConflictException("User already exists with email: " + request.email());
        }

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
    public UserResponseDTO createVisibleTo(CreateUserRequestDTO request, AuthUserDTO authUser)
    {
        if (authUser.role() == Role.MEMBER)
        {
            throw new ForbiddenException("Members are not allowed to create users");
        }

        if (authUser.role() == Role.COMPANY_ADMIN)
        {
            if(!request.companyId().equals(authUser.companyId()))
            {
                throw new ForbiddenException("Company admins can only create users in their own company");
            }
            if(request.role() == Role.SYSTEM_ADMIN)
            {
                throw new ForbiddenException("Company admins cannot create system admins");
            }
        }

        return create(request);
    }


    @Override
    public UserResponseDTO register(CreateUserRequestDTO request)
    {
        if (userDAO.findByEmail(request.email()).isPresent())
        {
            throw new ConflictException("User already exists with email: " + request.email());
        }

        Company company = companyDAO.getById(request.companyId());

        String hashedPassword = passwordService.hashPassword(request.password());

        User user = User.builder()
                .company(company)
                .email(request.email())
                .firstname(request.firstname())
                .lastname(request.lastname())
                .dob(request.dob())
                .role(Role.MEMBER)
                .passwordHash(hashedPassword)
                .build();

        User created = userDAO.create(user);
        User createdWithCompany = userDAO.getByIdWithCompany(created.getId());

        return mapToResponseDTO(createdWithCompany);
    }

    @Override
    public UserResponseDTO getByIdVisibleTo(Long id, AuthUserDTO authUser)
    {
        User user = userDAO.getByIdWithCompany(id);

        if (authUser.role() == Role.SYSTEM_ADMIN)
        {
            return mapToResponseDTO(user);
        }

        if (authUser.role() == Role.COMPANY_ADMIN)
        {
            if (!user.getCompany().getId().equals(authUser.companyId()))
            {
                throw new ForbiddenException("Company admins can only view users from their own company");
            }

            return mapToResponseDTO(user);
        }

        if (!user.getId().equals(authUser.userId()))
        {
            throw new ForbiddenException("Members can only view their own profile");
        }

        return mapToResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateVisibleTo(Long id, UpdateUserRequestDTO request, AuthUserDTO authUser)
    {
        User user = userDAO.getByIdWithCompany(id);

        if (authUser.role() == Role.COMPANY_ADMIN)
        {
            if (!user.getCompany().getId().equals(authUser.companyId()))
            {
                throw new ForbiddenException("Company admins can only update users from their own company");
            }

            if (user.getRole() == Role.SYSTEM_ADMIN && request.role() != Role.SYSTEM_ADMIN)
            {
                throw new ForbiddenException("Company admins cannot demote system admins");
            }

            if (request.role() == Role.SYSTEM_ADMIN)
            {
                throw new ForbiddenException("Company admins cannot promote users to system admin");
            }
        }

        if (authUser.role() == Role.MEMBER)
        {
            if (!user.getId().equals(authUser.userId()))
            {
                throw new ForbiddenException("Members can only update their own profile");
            }

            user.setFirstname(request.firstname());
            user.setLastname(request.lastname());
            user.setDob(request.dob());

            User updated = userDAO.update(user);
            User updatedWithCompany = userDAO.getByIdWithCompany(updated.getId());

            return mapToResponseDTO(updatedWithCompany);
        }

        user.setFirstname(request.firstname());
        user.setLastname(request.lastname());
        user.setDob(request.dob());
        user.setRole(request.role());

        User updated = userDAO.update(user);
        User updatedWithCompany = userDAO.getByIdWithCompany(updated.getId());

        return mapToResponseDTO(updatedWithCompany);
    }

    @Override
    public void deleteVisibleTo(Long id, AuthUserDTO authUser)
    {
        if (authUser.role() == Role.MEMBER)
        {
            throw new ForbiddenException("Members are not allowed to delete users");
        }

        User user = userDAO.getByIdWithCompany(id);

        if (authUser.role() == Role.COMPANY_ADMIN &&
                !user.getCompany().getId().equals(authUser.companyId()))
        {
            throw new ForbiddenException("Company admins can only delete users from their own company");
        }

        userDAO.delete(user);
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
