package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserWorkService {

    private static final Logger logger = LoggerFactory.getLogger(UserWorkService.class);
    private final UserWorkRepository userWorkRepository;
    private final WorkRepository workRepository;

    public UserWorkService(UserWorkRepository userWorkRepository, WorkRepository workRepository) {
        this.userWorkRepository = userWorkRepository;
        this.workRepository = workRepository;
    }

    @Transactional
    public UserWorkResponseDTO saveOrUpdate(Long workId, UserWorkRequestDTO dto, User user) {
        logger.info("User {} is updating collection status/rating for work ID {}", user.getUsername(), workId);
        UserWork userWork = userWorkRepository.findByUserIdAndWorkId(user.getId(), workId)
                .orElseGet(() -> createUserWork(workId, user));

        userWork.setStatus(dto.status());
        userWork.setRating(dto.rating());

        UserWork saved = userWorkRepository.save(userWork);
        logger.info("Collection status/rating successfully updated for user {} and work ID {}", user.getUsername(), workId);

        return convertToResponseDTO(saved);
    }

    @Transactional(readOnly = true)
    public UserWorkResponseDTO findByWorkIdAndUser(Long workId, User user) {
        logger.info("User {} is fetching collection status for work ID {}", user.getUsername(), workId);
        if (!workRepository.existsById(workId)) {
            throw new ResourceNotFoundException("Obra não encontrada");
        }

        UserWork userWork = userWorkRepository.findByUserIdAndWorkId(user.getId(), workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não adicionada à coleção do usuário"));

        return convertToResponseDTO(userWork);
    }

    @Transactional
    public void delete(Long workId, User user) {
        logger.info("User {} is removing work ID {} from collection", user.getUsername(), workId);
        if (!workRepository.existsById(workId)) {
            throw new ResourceNotFoundException("Obra não encontrada");
        }

        UserWork userWork = userWorkRepository.findByUserIdAndWorkId(user.getId(), workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não adicionada à coleção do usuário"));

        userWorkRepository.delete(userWork);
        logger.info("Work ID {} successfully removed from collection for user {}", workId, user.getUsername());
    }

    private UserWork createUserWork(Long workId, User user) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não encontrada"));

        UserWork uw = new UserWork();
        uw.setUser(user);
        uw.setWork(work);
        return uw;
    }

    private UserWorkResponseDTO convertToResponseDTO(UserWork userWork) {
        return new UserWorkResponseDTO(
                userWork.getId(),
                userWork.getWork().getId(),
                userWork.getWork().getTitle(),
                userWork.getStatus(),
                userWork.getRating()
        );
    }
}
