package io.github.leturnos.panelvault.controller.docs;

import io.github.leturnos.panelvault.dto.FinancialSummaryResponseDTO;
import io.github.leturnos.panelvault.dto.MonthlyExpenseResponseDTO;
import io.github.leturnos.panelvault.dto.WorkFinancialResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Finances", description = "Endpoints for tracking collection spending and financial projections")
@SecurityRequirement(name = "bearerAuth")
public interface FinancialControllerDoc {

    @Operation(
            summary = "Get financial summary",
            description = "Retrieves an overview of the authenticated user's collection spending, including total invested, average price per volume, highest and lowest prices paid, and an estimated cost to complete the entire collection."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Financial summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/summary")
    ResponseEntity<FinancialSummaryResponseDTO> summary(@Parameter(hidden = true) @AuthenticationPrincipal User user);

    @Operation(
            summary = "Get monthly spending history",
            description = "Returns the authenticated user's purchase history grouped by month. " +
                    "Filter by a specific year using the 'year' parameter, or by an arbitrary date range using 'startDate' and 'endDate'. " +
                    "If no filter is provided, the full history is returned."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Monthly expense history retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/history")
    ResponseEntity<List<MonthlyExpenseResponseDTO>> history(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "Filter by year (e.g. 2026). Ignored if startDate or endDate are provided.")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "Start of the date range (inclusive). Format: yyyy-MM-dd.")
            @RequestParam(required = false) LocalDate startDate,
            @Parameter(description = "End of the date range (inclusive). Format: yyyy-MM-dd.")
            @RequestParam(required = false) LocalDate endDate
    );

    @Operation(
            summary = "List financial summary by work",
            description = "Returns a paginated list of all works in the authenticated user's collection with their respective financial data, such as total spent and estimated cost to complete each work."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Works financial summary retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token")
    })
    @GetMapping("/works")
    ResponseEntity<Page<WorkFinancialResponseDTO>> summaryWorks(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @ParameterObject Pageable pageable
    );

    @Operation(
            summary = "Get financial details of a specific work",
            description = "Returns detailed financial data for a single work in the authenticated user's collection: total spent, average price paid per volume, number of owned volumes, and the estimated cost to acquire the remaining volumes."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Work financial details retrieved successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - invalid or missing JWT token"),
            @ApiResponse(responseCode = "404", description = "Work not found in the user's collection")
    })
    @GetMapping("/works/{workId}")
    ResponseEntity<WorkFinancialResponseDTO> summaryByWork(
            @Parameter(hidden = true) @AuthenticationPrincipal User user,
            @Parameter(description = "ID of the work") @PathVariable Long workId
    );
}
