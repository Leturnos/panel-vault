package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
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

import java.util.List;

@Tag(name = "Volumes", description = "Endpoints for managing manga/comic volumes")
@SecurityRequirement(name = "bearerAuth")
public interface VolumeControllerDoc {

    @Operation(summary = "Add a volume to a work", description = "Creates and associates a new volume with the specified work")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Volume created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @PostMapping("/works/{id}/volumes")
    ResponseEntity<VolumeResponseDTO> create(
            @Parameter(description = "ID of the work") @PathVariable Long id,
            @Valid @RequestBody VolumeRequestDTO data,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(summary = "List all volumes of a work", description = "Retrieves all volumes associated with the specified work")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Volumes retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @GetMapping("/works/{workId}/volumes")
    ResponseEntity<List<VolumeResponseDTO>> findAllByWorkId(
            @Parameter(description = "ID of the work") @PathVariable Long workId,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(summary = "Get volume by ID", description = "Retrieves details of a specific volume by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Volume retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Volume not found")
    })
    @GetMapping("/volumes/{id}")
    ResponseEntity<VolumeResponseDTO> findById(
            @Parameter(description = "ID of the volume") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(summary = "Delete volume by ID", description = "Deletes a specific volume by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Volume deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Volume not found")
    })
    @DeleteMapping("/volumes/{id}")
    ResponseEntity<Void> delete(
            @Parameter(description = "ID of the volume") @PathVariable Long id,
            @Parameter(hidden = true) @AuthenticationPrincipal User user);
}
