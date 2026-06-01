package app.interfaces;

import app.dto.login.AuthUserDTO;
import app.dto.user.CreateUserRequestDTO;
import app.dto.user.UpdateUserRequestDTO;
import app.dto.user.UserResponseDTO;

import java.util.List;

public interface IUserService
{
    UserResponseDTO create(CreateUserRequestDTO request);

    UserResponseDTO register(CreateUserRequestDTO request);

    UserResponseDTO getById(Long id);

    List<UserResponseDTO> getAll();

    List<UserResponseDTO> getAllVisibleTo(AuthUserDTO authUser);

    UserResponseDTO update(Long id, UpdateUserRequestDTO request);

    void delete(Long id);
}