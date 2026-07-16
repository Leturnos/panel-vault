package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.LoginRequestDTO;
import io.github.leturnos.panelvault.dto.LoginResponseDTO;
import io.github.leturnos.panelvault.dto.RegisterRequestDTO;
import io.github.leturnos.panelvault.dto.UserResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.config.TokenService;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    private final AuthenticationManager authenticationManager;
    private final UserRepository repository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;
    private final TokenService tokenService;

    public UserService(AuthenticationManager authenticationManager, UserRepository repository, BCryptPasswordEncoder bCryptPasswordEncoder, TokenService tokenService) {
        this.authenticationManager = authenticationManager;
        this.repository = repository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public UserResponseDTO register(RegisterRequestDTO data) {
        logger.info("Attempting to register a new user with username: {}", data.username());
        if (repository.existsByEmailOrUsername(data.email(), data.username())) {
            logger.warn("Failed to create user. email: {} or username: {} already exists", data.email(), data.username());
            throw new DuplicateResourceException("Já existe um usuário cadastrado com email ou username.");
        }

        User user = new User(data);
        user.setPassword(bCryptPasswordEncoder.encode(data.password()));
        User savedUser = repository.save(user);
        logger.info("User successfully registered with ID: {}", savedUser.getId());
        return convertToResponseDTO(savedUser);
    }

    @Transactional
    public LoginResponseDTO login(LoginRequestDTO data) {
        logger.info("Attempting to authenticate user: {}", data.username());
        Authentication authenticationRequest = UsernamePasswordAuthenticationToken.unauthenticated(data.username(), data.password());
        Authentication authenticationResponse = authenticationManager.authenticate(authenticationRequest);

        org.springframework.security.core.userdetails.User userDetails = (org.springframework.security.core.userdetails.User) authenticationResponse.getPrincipal();

        User user = repository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new DuplicateResourceException("Usuário autenticado não pôde ser encontrado no banco de dados."));

        String token = tokenService.generateToken(user);
        logger.info("User {} successfully authenticated.", data.username());
        return new LoginResponseDTO(token);
    }

    private UserResponseDTO convertToResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail()
        );
    }
}
