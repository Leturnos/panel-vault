package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WorkService {

    private static final Logger logger = LoggerFactory.getLogger(WorkService.class);
    private final WorkRepository repository;

    public WorkService(WorkRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public WorkResponseDTO create(WorkRequestDTO data) {
        logger.info("Attempting to create a new work with title: {}", data.title());
        if (repository.existsByTitle(data.title())) {
            logger.warn("Failed to create work. Title already exists: {}", data.title());
            throw new DuplicateResourceException("Já existe uma obra cadastrada com este título.");
        }

        Work work = repository.save(new Work(data));
        logger.info("Work successfully created with ID: {}", work.getId());
        return convertToResponseDTO(work);
    }

    @Transactional(readOnly = true)
    public Page<WorkResponseDTO> findAll(String title, Pageable pageable) {
        logger.info("Fetching works. Title filter: {}, Pageable: {}", title, pageable);
        Page<Work> worksPage;
        if (title == null || title.isBlank()) {
            worksPage = repository.findAll(pageable);
        } else {
            worksPage = repository.findByTitleContainingIgnoreCase(title, pageable);
        }
        return worksPage.map(this::convertToResponseDTO);
    }

    @Transactional(readOnly = true)
    public WorkResponseDTO findById(Long id) {
        logger.info("Fetching work with ID: {}", id);
        Work work = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Work with ID {} not found.", id);
                    return new ResourceNotFoundException("Obra não encontrada");
                });
        return convertToResponseDTO(work);
    }

    @Transactional
    public WorkResponseDTO update(Long id, WorkRequestDTO data) {
        logger.info("Attempting to update work with ID: {}", id);
        Work work = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Failed to update. Work with ID {} not found.", id);
                    return new ResourceNotFoundException("Obra não encontrada");
                });

        if (repository.existsByTitle(data.title())) {
            logger.warn("Failed to update work. Title already exists: {}", data.title());
            throw new DuplicateResourceException("Já existe uma obra cadastrada com este título.");
        }

        work.setTitle(data.title());
        work.setType(data.type());
        work.setPublisher(data.publisher());
        work.setAuthor(data.author());
        work.setTotalVolumes(data.totalVolumes());
        work.setStatus(data.status());
        work.setCoverUrl(data.coverUrl());

        repository.save(work);
        logger.info("Work with ID {} successfully updated.", id);
        return convertToResponseDTO(work);
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Attempting to delete work with ID: {}", id);
        Work work = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Failed to delete. Work with ID {} not found.", id);
                    return new ResourceNotFoundException("Obra não encontrada");
                });
        repository.delete(work);
        logger.info("Work with ID {} successfully deleted.", id);
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
