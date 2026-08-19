package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Works", description = "Endpoints for managing manga/comic works")
@SecurityRequirement(name = "bearerAuth")
public interface WorkControllerDoc {

    @Operation(summary = "List works with pagination and filter", description = "Retrieves a paginated list of works, optionally filtered by title")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Works retrieved successfully")
    })
    @GetMapping
    ResponseEntity<Page<WorkResponseDTO>> findAll(
            @Parameter(description = "Filter works by title (case-insensitive contains)") @RequestParam(required = false) String title,
            @ParameterObject Pageable pageable);

    @Operation(summary = "Get work by ID", description = "Retrieves details of a specific work by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @GetMapping("/{id}")
    ResponseEntity<WorkResponseDTO> findById(@Parameter(description = "ID of the work") @PathVariable Long id);

    @Operation(summary = "Create a new work", description = "Creates a new work in the catalog")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Work created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @PostMapping
    ResponseEntity<WorkResponseDTO> create(@Valid @RequestBody WorkRequestDTO data);

    @Operation(summary = "Update a work", description = "Updates details of an existing work by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload or validation error"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @PutMapping("/{id}")
    ResponseEntity<WorkResponseDTO> update(
            @Parameter(description = "ID of the work") @PathVariable Long id,
            @Valid @RequestBody WorkRequestDTO data);

    @Operation(summary = "Delete a work", description = "Deletes a specific work by its ID")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Work deleted successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found")
    })
    @DeleteMapping("/{id}")
    ResponseEntity<Void> delete(@Parameter(description = "ID of the work") @PathVariable Long id);
}
