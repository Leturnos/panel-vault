package io.github.leturnos.panelvault.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.leturnos.panelvault.config.TokenService;
import io.github.leturnos.panelvault.dto.LoginRequestDTO;
import io.github.leturnos.panelvault.dto.LoginResponseDTO;
import io.github.leturnos.panelvault.dto.RegisterRequestDTO;
import io.github.leturnos.panelvault.dto.UserResponseDTO;
import io.github.leturnos.panelvault.repository.UserRepository;
import io.github.leturnos.panelvault.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@WithMockUser
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService service;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private UserRepository userRepository;

    @MockitoBean
    private UserDetailsService userDetailsService;

    @Nested
    @DisplayName("POST /auth/register tests")
    class RegisterTests {

        @Test
        @DisplayName("Should return 201 Created with Location header when valid registration data is sent")
        void register_shouldReturn201WithLocationHeader_whenDataIsValid() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("validuser", "valid@example.com", "password123");
            UserResponseDTO response = new UserResponseDTO(1L, "validuser", "valid@example.com");

            Mockito.when(service.register(any(RegisterRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(header().string("Location", containsString("/auth/register/1")))
                    .andExpect(jsonPath("$.id", is(1)))
                    .andExpect(jsonPath("$.username", is("validuser")))
                    .andExpect(jsonPath("$.email", is("valid@example.com")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when email format is invalid")
        void register_shouldReturn400_whenEmailIsInvalid() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("validuser", "not-an-email", "password123");

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("email")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when password has fewer than 8 characters")
        void register_shouldReturn400_whenPasswordIsTooShort() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("validuser", "valid@example.com", "short");

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("password")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when username is blank")
        void register_shouldReturn400_whenUsernameIsBlank() throws Exception {
            RegisterRequestDTO request = new RegisterRequestDTO("   ", "valid@example.com", "password123");

            mockMvc.perform(post("/auth/register")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItem("username")));
        }
    }

    @Nested
    @DisplayName("POST /auth/login tests")
    class LoginTests {

        @Test
        @DisplayName("Should return 200 OK with token when credentials are valid")
        void login_shouldReturn200WithToken_whenCredentialsAreValid() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO("validuser", "password123");
            LoginResponseDTO response = new LoginResponseDTO("mocked-jwt-token");

            Mockito.when(service.login(any(LoginRequestDTO.class))).thenReturn(response);

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.token", is("mocked-jwt-token")));
        }

        @Test
        @DisplayName("Should return 400 Bad Request when username or password is blank")
        void login_shouldReturn400_whenCredentialsAreBlank() throws Exception {
            LoginRequestDTO request = new LoginRequestDTO("", "");

            mockMvc.perform(post("/auth/login")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title", is("Falha na Validação")))
                    .andExpect(jsonPath("$.errors[*].field", hasItems("username", "password")));
        }
    }
}
