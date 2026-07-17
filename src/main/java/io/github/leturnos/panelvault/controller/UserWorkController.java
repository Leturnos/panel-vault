package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.service.UserWorkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/works/{workId}/collection")
public class UserWorkController {

    private final UserWorkService service;

    public UserWorkController(UserWorkService service) {
        this.service = service;
    }

    @PutMapping
    public ResponseEntity<UserWorkResponseDTO> saveOrUpdate(
            @PathVariable Long workId,
            @Valid @RequestBody UserWorkRequestDTO data,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.saveOrUpdate(workId, data, user));
    }

    @GetMapping
    public ResponseEntity<UserWorkResponseDTO> findByWorkIdAndUser(
            @PathVariable Long workId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findByWorkIdAndUser(workId, user));
    }

    @DeleteMapping
    public ResponseEntity<Void> delete(@PathVariable Long workId, @AuthenticationPrincipal User user) {
        service.delete(workId, user);
        return ResponseEntity.noContent().build();
    }
}
