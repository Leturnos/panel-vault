package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.LoginRequestDTO;
import io.github.leturnos.panelvault.dto.LoginResponseDTO;
import io.github.leturnos.panelvault.dto.RegisterRequestDTO;
import io.github.leturnos.panelvault.dto.UserResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private UserRepository repository;

    @Mock
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserService userService;

    @Nested
    @DisplayName("Registration tests")
    class RegisterTests {

        @Test
        @DisplayName("Should successfully register a new user when email and username are unique")
        void register_shouldHashPasswordAndSaveUser_whenDataIsValid() {
            RegisterRequestDTO request = new RegisterRequestDTO("newuser", "newuser@example.com", "password123");

            when(repository.existsByEmailOrUsername("newuser@example.com", "newuser")).thenReturn(false);
            when(bCryptPasswordEncoder.encode("password123")).thenReturn("hashedPassword");
            when(repository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId(1L);
                return user;
            });

            UserResponseDTO response = userService.register(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.username()).isEqualTo("newuser");
            assertThat(response.email()).isEqualTo("newuser@example.com");

            verify(repository).existsByEmailOrUsername("newuser@example.com", "newuser");
            verify(bCryptPasswordEncoder).encode("password123");
            verify(repository).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when email or username already exists")
        void register_shouldThrowDuplicateResourceException_whenEmailOrUsernameAlreadyExists() {
            RegisterRequestDTO request = new RegisterRequestDTO("existinguser", "existing@example.com", "password123");

            when(repository.existsByEmailOrUsername("existing@example.com", "existinguser")).thenReturn(true);

            assertThatThrownBy(() -> userService.register(request))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Já existe um usuário cadastrado com email ou username.");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Login tests")
    class LoginTests {

        @Test
        @DisplayName("Should authenticate and return JWT token when credentials are valid")
        void login_shouldAuthenticateAndReturnToken_whenCredentialsAreValid() {
            LoginRequestDTO request = new LoginRequestDTO("validuser", "password123");
            org.springframework.security.core.userdetails.User springUser =
                    new org.springframework.security.core.userdetails.User("validuser", "password123", Collections.emptyList());
            Authentication authResponse = new UsernamePasswordAuthenticationToken(springUser, "password123", Collections.emptyList());

            User domainUser = new User();
            domainUser.setId(1L);
            domainUser.setUsername("validuser");
            domainUser.setEmail("validuser@example.com");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResponse);
            when(repository.findByUsername("validuser")).thenReturn(Optional.of(domainUser));
            when(tokenService.generateToken(domainUser)).thenReturn("mocked.jwt.token");

            LoginResponseDTO response = userService.login(request);

            assertThat(response).isNotNull();
            assertThat(response.token()).isEqualTo("mocked.jwt.token");
            verify(tokenService).generateToken(domainUser);
        }

        @Test
        @DisplayName("Should throw BadCredentialsException when authentication fails")
        void login_shouldThrowBadCredentialsException_whenCredentialsAreInvalid() {
            LoginRequestDTO request = new LoginRequestDTO("invaliduser", "wrongpassword");

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                    .thenThrow(new BadCredentialsException("Bad credentials"));

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(BadCredentialsException.class)
                    .hasMessage("Bad credentials");

            verify(tokenService, never()).generateToken(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when authenticated user is missing from database")
        void login_shouldThrowException_whenAuthenticatedUserNotFoundInDatabase() {
            LoginRequestDTO request = new LoginRequestDTO("ghostuser", "password123");
            org.springframework.security.core.userdetails.User springUser =
                    new org.springframework.security.core.userdetails.User("ghostuser", "password123", Collections.emptyList());
            Authentication authResponse = new UsernamePasswordAuthenticationToken(springUser, "password123", Collections.emptyList());

            when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authResponse);
            when(repository.findByUsername("ghostuser")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.login(request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Usuário autenticado não pôde ser encontrado no banco de dados.");
        }
    }
}
