package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.LoginRequestDTO;
import io.github.leturnos.panelvault.dto.LoginResponseDTO;
import io.github.leturnos.panelvault.dto.RegisterRequestDTO;
import io.github.leturnos.panelvault.dto.UserResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Tag(name = "Authentication", description = "Endpoints for user registration and authentication")
public interface UserControllerDoc {

    @Operation(summary = "Register a new user", description = "Creates a new user account in the system")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "409", description = "User with given username or email already exists")
    })
    @PostMapping("/register")
    ResponseEntity<UserResponseDTO> register(@Valid @RequestBody RegisterRequestDTO data);

    @Operation(summary = "User login", description = "Authenticates user credentials and returns a JWT access token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Authentication successful"),
            @ApiResponse(responseCode = "400", description = "Invalid credentials or validation error")
    })
    @PostMapping("/login")
    ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO data);
}
