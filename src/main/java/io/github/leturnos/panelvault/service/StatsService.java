package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.StatsResponseDTO;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.springframework.stereotype.Service;

@Service
public class StatsService {

    private final WorkRepository workRepository;
    private final VolumeRepository volumeRepository;

    public StatsService(WorkRepository workRepository, VolumeRepository volumeRepository) {
        this.workRepository = workRepository;
        this.volumeRepository = volumeRepository;
    }

    public StatsResponseDTO getStats() {
        long totalWorks = workRepository.count();
        long totalVolumes = volumeRepository.countByOwnedTrue();
        long completedWorks = workRepository.countByStatus(WorkStatus.COMPLETED);
        long onGoingWorks = workRepository.countByStatus(WorkStatus.ONGOING);
        long wishlistItems = workRepository.countByStatus(WorkStatus.WISHLIST);
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
}
