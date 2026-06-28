package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.service.WorkService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/works")
public class WorkController {

    private final WorkService service;

    public WorkController(WorkService service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<List<WorkResponseDTO>> findAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<WorkResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    public ResponseEntity<WorkResponseDTO> create(@Valid @RequestBody WorkRequestDTO data) {
        WorkResponseDTO result = service.create(data);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(uri).body(result);
    }

    @PutMapping("/{id}")
    public ResponseEntity<WorkResponseDTO> update(@PathVariable Long id, @Valid @RequestBody WorkRequestDTO data) {
        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
