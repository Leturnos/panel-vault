package io.github.leturnos.panelvault.controller;

import io.github.leturnos.panelvault.controller.docs.WorkControllerDoc;
import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.service.WorkService;
import jakarta.validation.Valid;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/works")
public class WorkController implements WorkControllerDoc {

    private final WorkService service;

    public WorkController(WorkService service) {
        this.service = service;
    }

    @GetMapping
    @Override
    public ResponseEntity<Page<WorkResponseDTO>> findAll(
            @RequestParam(required = false) String title,
            @ParameterObject Pageable pageable) {
        return ResponseEntity.ok(service.findAll(title, pageable));
    }

    @GetMapping("/{id}")
    @Override
    public ResponseEntity<WorkResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(service.findById(id));
    }

    @PostMapping
    @Override
    public ResponseEntity<WorkResponseDTO> create(@Valid @RequestBody WorkRequestDTO data) {
        WorkResponseDTO result = service.create(data);

        URI uri = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(result.id())
                .toUri();

        return ResponseEntity.created(uri).body(result);
    }

    @PutMapping("/{id}")
    @Override
    public ResponseEntity<WorkResponseDTO> update(@PathVariable Long id, @Valid @RequestBody WorkRequestDTO data) {
        return ResponseEntity.ok(service.update(id, data));
    }

    @DeleteMapping("/{id}")
    @Override
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.noContent().build();
    }
}
