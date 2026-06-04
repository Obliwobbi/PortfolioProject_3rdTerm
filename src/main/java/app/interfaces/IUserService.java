package app.interfaces;

import app.dto.login.AuthUserDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;

import java.util.List;

public interface IUserService
{
    List<UserResponseDTO> getAll();

    List<UserResponseDTO> getAllVisibleTo(AuthUserDTO authUser);

    UserResponseDTO create(CreateUserRequestDTO request);

    UserResponseDTO createVisibleTo(CreateUserRequestDTO request, AuthUserDTO authUser);

    UserResponseDTO register(CreateUserRequestDTO request);

    UserResponseDTO getByIdVisibleTo(Long id, AuthUserDTO authUser);

    UserResponseDTO updateVisibleTo(Long id, UpdateUserRequestDTO request, AuthUserDTO authUser);

    void deleteVisibleTo(Long id, AuthUserDTO authUser);
}