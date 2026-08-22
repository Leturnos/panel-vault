package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.controller.docs.FinancialControllerDoc;
import io.github.leturnos.panelvault.dto.FinancialSummaryResponseDTO;
import io.github.leturnos.panelvault.dto.MonthlyExpenseResponseDTO;
import io.github.leturnos.panelvault.dto.WorkFinancialResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.service.FinancialService;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/finances")
public class FinancialController implements FinancialControllerDoc {

    private final FinancialService service;

    public FinancialController(FinancialService service) {
        this.service = service;
    }

    @GetMapping("/summary")
    @Override
    public ResponseEntity<FinancialSummaryResponseDTO> summary(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.getSummary(user));
    }

    @GetMapping("/history")
    @Override
    public ResponseEntity<List<MonthlyExpenseResponseDTO>> history(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) LocalDate startDate,
            @RequestParam(required = false) LocalDate endDate
    ) {
        return ResponseEntity.ok(service.getSummaryByDate(user, year, startDate, endDate));
    }

    @GetMapping("/works")
    @Override
    public ResponseEntity<Page<WorkFinancialResponseDTO>> summaryWorks(
            @AuthenticationPrincipal User user,
            @ParameterObject Pageable pageable
    ) {
        return ResponseEntity.ok(service.getSummaryByUser(user, pageable));
    }

    @GetMapping("/works/{workId}")
    @Override
    public ResponseEntity<WorkFinancialResponseDTO> summaryByWork(
            @AuthenticationPrincipal User user,
            @PathVariable Long workId) {
        return ResponseEntity.ok(service.getSummaryByWork(user, workId));
    }
}
