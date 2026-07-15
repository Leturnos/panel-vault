package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.UserRequestDTO;
import io.github.leturnos.panelvault.dto.UserResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserService(UserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    public UserResponseDTO register(UserRequestDTO data) {
        logger.info("Attempting to register a new user with username: {}", data.username());
        if (repository.existsByEmailOrUsername(data.email(), data.username())){
            logger.warn("Failed to create user. email: {} or username: {} already exists", data.email(), data.username());
            throw new DuplicateResourceException("Já existe um usuário cadastrado com email ou username.");
        }

        User user = new User(data);
        user.setPassword(bCryptPasswordEncoder.encode(data.password()));
        User savedUser = repository.save(user);
        logger.info("User successfully registered with ID: {}", savedUser.getId());
        return convertToResponseDTO(savedUser);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
