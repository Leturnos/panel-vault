package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class WorkService {

    private final WorkRepository repository;

    public WorkService(WorkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkResponseDTO create(WorkRequestDTO data) {
        Work work = repository.save(new Work(data));
        return convertToResponseDTO(work);
    }

    @Transactional(readOnly = true)
    public List<WorkResponseDTO> findAll() {
        return repository.findAll()
                .stream()
                .map(this::convertToResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public WorkResponseDTO findById(Long id) {
        Work work = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra não encontrada"));
        return convertToResponseDTO(work);
    }

    @Transactional
    public WorkResponseDTO update(Long id, WorkRequestDTO data) {
        Work work = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra não encontrada"));

        work.setTitle(data.title());
        work.setType(data.type());
        work.setPublisher(data.publisher());
        work.setAuthor(data.author());
        work.setTotalVolumes(data.totalVolumes());
        work.setStatus(data.status());
        work.setCoverUrl(data.coverUrl());

        repository.save(work);
        return convertToResponseDTO(work);
    }

    @Transactional
    public void delete(Long id) {
        Work work = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Obra não encontrada"));
        repository.delete(work);
    }

    private WorkResponseDTO convertToResponseDTO(Work work) {
        return new WorkResponseDTO(
                work.getId(),
                work.getTitle(),
                work.getType(),
                work.getPublisher(),
                work.getAuthor(),
                work.getTotalVolumes(),
                work.getStatus(),
                work.getCoverUrl()
        );
    }
}
