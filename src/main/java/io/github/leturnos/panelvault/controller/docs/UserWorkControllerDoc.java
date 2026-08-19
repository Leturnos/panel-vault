package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "User Collection", description = "Endpoints for managing a user's tracking and relationship with a work")
@SecurityRequirement(name = "bearerAuth")
public interface UserWorkControllerDoc {

    @Operation(summary = "Add or update work in user collection", description = "Saves or updates tracking details (reading status, rating, etc.) for a work in the authenticated user's collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection entry saved or updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @PutMapping
    ResponseEntity<UserWorkResponseDTO> saveOrUpdate(
            @Parameter(description = "ID of the work") @PathVariable Long workId,
            @Valid @RequestBody UserWorkRequestDTO data,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(summary = "Get user collection details for a work", description = "Retrieves user-specific tracking details for the specified work")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Collection entry retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work or collection entry not found")
    })
    @GetMapping
    ResponseEntity<UserWorkResponseDTO> findByWorkIdAndUser(
            @Parameter(description = "ID of the work") @PathVariable Long workId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(summary = "Remove work from user collection", description = "Removes a work from the authenticated user's collection")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Work removed from collection successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Collection entry not found")
    })
    @DeleteMapping
    ResponseEntity<Void> delete(
            @Parameter(description = "ID of the work") @PathVariable Long workId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);
}
