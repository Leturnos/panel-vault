package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.StatsResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;

@Tag(name = "Statistics", description = "Endpoints for general and user-specific collection statistics")
public interface StatsControllerDoc {

    @Operation(summary = "Get global statistics", description = "Retrieves general system statistics such as total works and registered users")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Global statistics retrieved successfully")
    })
    @GetMapping
    ResponseEntity<StatsResponseDTO> stats();

    @Operation(
            summary = "Get current user statistics",
            description = "Retrieves statistics specific to the authenticated user's collection",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User statistics retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/me")
    ResponseEntity<StatsResponseDTO> statsMe(@Parameter(hidden = true) @AuthenticationPrincipal User user);
}
