package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.service.VolumeService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
public class VolumeController {

    private final VolumeService service;

    public VolumeController(VolumeService service) {
        this.service = service;
    }

    @PostMapping("/works/{id}/volumes")
    public ResponseEntity<VolumeResponseDTO> create(
            @PathVariable Long id,
            @Valid @RequestBody VolumeRequestDTO data,
            @AuthenticationPrincipal User user) {
        VolumeResponseDTO result = service.create(id, data, user);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(uri).body(result);
    }

    @GetMapping("/works/{workId}/volumes")
    public ResponseEntity<List<VolumeResponseDTO>> findAllByWorkId(
            @PathVariable Long workId,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findAllByWorkId(workId, user));
    }

    @GetMapping("/volumes/{id}")
    public ResponseEntity<VolumeResponseDTO> findById(@PathVariable Long id, @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(service.findById(id, user));
    }

    @DeleteMapping("/volumes/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal User user) {
        service.delete(id, user);
        return ResponseEntity.noContent().build();
    }
}
