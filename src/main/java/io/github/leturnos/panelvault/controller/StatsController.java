package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.StatsResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.service.StatsService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/stats")
public class StatsController {

    private final StatsService service;

    public StatsController(StatsService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<StatsResponseDTO> stats() {
        StatsResponseDTO response = service.getStats();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<StatsResponseDTO> statsMe(@AuthenticationPrincipal User user) {
        StatsResponseDTO response = service.getUserStats(user);
        return ResponseEntity.ok(response);
    }
}
