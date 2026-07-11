package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.Volume;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class VolumeService {

    private static final Logger logger = LoggerFactory.getLogger(VolumeService.class);
    private final VolumeRepository repository;
    private final WorkRepository workRepository;

    public VolumeService(VolumeRepository repository, WorkRepository workRepository) {
        this.repository = repository;
        this.workRepository = workRepository;
    }

    @Transactional
    public VolumeResponseDTO create(Long workId, VolumeRequestDTO data) {
        logger.info("Creating volume number {} for work with ID {}", data.number(), workId);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não encontrada"));

        Volume volume = new Volume(data);
        volume.setWork(work);

        Volume savedVolume = repository.save(volume);
        logger.info("Volume with ID {} successfully created for work with ID {}", savedVolume.getId(), workId);
        return convertToResponseDTO(savedVolume);
    }

    @Transactional(readOnly = true)
    public List<VolumeResponseDTO> findAllByWorkId(Long workId) {
        logger.info("Searching for all volumes associated with work ID {}", workId);
        if (!workRepository.existsById(workId)) {
            logger.warn("Volume search failed: Work with ID {} not found", workId);
            throw new ResourceNotFoundException("Obra não encontrada");
        }

        List<Volume> volumes = repository.findByWorkId(workId);
        logger.info("Found {} volumes for work with ID {}", volumes.size(), workId);
        return volumes.stream()
            .map(this::convertToResponseDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public VolumeResponseDTO findById(Long volumeId) {
        logger.info("Searching for volume with ID {}", volumeId);
        Volume volume = repository.findById(volumeId)
                .orElseThrow(() -> {
                    logger.warn("Volume search failed: Volume with ID {} not found", volumeId);
                    return new ResourceNotFoundException("Volume não encontrado");
                });
        return convertToResponseDTO(volume);
    }

    @Transactional
    public void delete(Long id) {
        logger.info("Deleting volume with ID {}", id);
        Volume volume = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Volume deletion failed: Volume with ID {} not found", id);
                    return new ResourceNotFoundException("Volume não encontrado");
                });
        repository.delete(volume);
        logger.info("Volume with ID {} successfully deleted", id);
    }

    private VolumeResponseDTO convertToResponseDTO(Volume volume) {
        return new VolumeResponseDTO(
                volume.getId(),
                volume.getNumber(),
                volume.getPurchaseDate(),
                volume.getPurchasePrice(),
                volume.getOwned(),
                volume.getWork()
        );
    }
}
