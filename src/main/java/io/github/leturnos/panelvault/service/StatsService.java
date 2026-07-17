package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.StatsResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StatsService {

    private final WorkRepository workRepository;
    private final VolumeRepository volumeRepository;
    private final UserWorkRepository userWorkRepository;

    public StatsService(WorkRepository workRepository, VolumeRepository volumeRepository, UserWorkRepository userWorkRepository) {
        this.workRepository = workRepository;
        this.volumeRepository = volumeRepository;
        this.userWorkRepository = userWorkRepository;
    }

    @Transactional(readOnly = true)
    public StatsResponseDTO getStats() {
        long totalWorks = workRepository.count();
        long totalVolumes = volumeRepository.countByOwnedTrue();
        long completedWorks = userWorkRepository.countByStatus(WorkStatus.COMPLETED);
        long onGoingWorks = userWorkRepository.countByStatus(WorkStatus.ONGOING);
        long wishlistItems = userWorkRepository.countByStatus(WorkStatus.WISHLIST);
        long totalMangas = workRepository.countByType(WorkType.MANGA);
        long totalComics = workRepository.countByType(WorkType.COMIC);
        long totalGraphicNovels = workRepository.countByType(WorkType.GRAPHIC_NOVEL);
        long totalManhwas = workRepository.countByType(WorkType.MANHWA);

        return new StatsResponseDTO(
                totalWorks,
                totalVolumes,
                completedWorks,
                onGoingWorks,
                wishlistItems,
                totalMangas,
                totalComics,
                totalGraphicNovels,
                totalManhwas
        );
    }

    @Transactional(readOnly = true)
    public StatsResponseDTO getUserStats(User user) {
        long totalWorks = userWorkRepository.countByUserId(user.getId());
        long totalVolumes = volumeRepository.countByUserIdAndOwnedTrue(user.getId());
        long completedWorks = userWorkRepository.countByUserIdAndStatus(user.getId(), WorkStatus.COMPLETED);
        long onGoingWorks = userWorkRepository.countByUserIdAndStatus(user.getId(), WorkStatus.ONGOING);
        long wishlistItems = userWorkRepository.countByUserIdAndStatus(user.getId(), WorkStatus.WISHLIST);
        long totalMangas = userWorkRepository.countByUserIdAndWorkType(user.getId(), WorkType.MANGA);
        long totalComics = userWorkRepository.countByUserIdAndWorkType(user.getId(), WorkType.COMIC);
        long totalGraphicNovels = userWorkRepository.countByUserIdAndWorkType(user.getId(), WorkType.GRAPHIC_NOVEL);
        long totalManhwas = userWorkRepository.countByUserIdAndWorkType(user.getId(), WorkType.MANHWA);

        return new StatsResponseDTO(
                totalWorks,
                totalVolumes,
                completedWorks,
                onGoingWorks,
                wishlistItems,
                totalMangas,
                totalComics,
                totalGraphicNovels,
                totalManhwas
        );
    }
}
