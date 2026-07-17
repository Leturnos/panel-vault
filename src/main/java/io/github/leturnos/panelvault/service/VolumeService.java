package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
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
    public VolumeResponseDTO create(Long workId, VolumeRequestDTO data, User user) {
        logger.info("User {} is creating volume number {} for work with ID {}", user.getUsername(), data.number(), workId);
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não encontrada"));

        Volume volume = new Volume(data);
        volume.setWork(work);
        volume.setUser(user);

        Volume savedVolume = repository.save(volume);
        logger.info("Volume with ID {} successfully created for work with ID {} by user {}", savedVolume.getId(), workId, user.getUsername());
        return convertToResponseDTO(savedVolume);
    }

    @Transactional(readOnly = true)
    public List<VolumeResponseDTO> findAllByWorkId(Long workId, User user) {
        logger.info("User {} is searching for all volumes associated with work ID {}", user.getUsername(), workId);
        if (!workRepository.existsById(workId)) {
            logger.warn("Volume search failed: Work with ID {} not found", workId);
            throw new ResourceNotFoundException("Obra não encontrada");
        }

        List<Volume> volumes = repository.findByWorkIdAndUserId(workId, user.getId());
        logger.info("Found {} volumes for work with ID {} belonging to user {}", volumes.size(), workId, user.getUsername());
        return volumes.stream()
            .map(this::convertToResponseDTO)
            .toList();
    }

    @Transactional(readOnly = true)
    public VolumeResponseDTO findById(Long volumeId, User user) {
        logger.info("User {} is searching for volume with ID {}", user.getUsername(), volumeId);
        Volume volume = repository.findById(volumeId)
                .orElseThrow(() -> {
                    logger.warn("Volume search failed: Volume with ID {} not found", volumeId);
                    return new ResourceNotFoundException("Volume não encontrado");
                });

        if (!volume.getUser().getId().equals(user.getId())) {
            logger.warn("Volume search failed: Volume with ID {} does not belong to user {}", volumeId, user.getUsername());
            throw new ResourceNotFoundException("Volume não encontrado");
        }

        return convertToResponseDTO(volume);
    }

    @Transactional
    public void delete(Long id, User user) {
        logger.info("User {} is deleting volume with ID {}", user.getUsername(), id);
        Volume volume = repository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Volume deletion failed: Volume with ID {} not found", id);
                    return new ResourceNotFoundException("Volume não encontrado");
                });

        if (!volume.getUser().getId().equals(user.getId())) {
            logger.warn("Volume deletion failed: Volume with ID {} does not belong to user {}", id, user.getUsername());
            throw new ResourceNotFoundException("Volume não encontrado");
        }

        repository.delete(volume);
        logger.info("Volume with ID {} successfully deleted by user {}", id, user.getUsername());
    }

    private VolumeResponseDTO convertToResponseDTO(Volume volume) {
        return new VolumeResponseDTO(
                volume.getId(),
                volume.getNumber(),
                volume.getPurchaseDate(),
                volume.getPurchasePrice(),
                volume.getOwned(),
                volume.getWork().getId()
        );
    }
}
