package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.StatsResponseDTO;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private VolumeRepository volumeRepository;

    @Mock
    private UserWorkRepository userWorkRepository;

    @InjectMocks
    private StatsService statsService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("statuser");
    }

    @Test
    @DisplayName("Should return correct aggregated global statistics")
    void getStats_shouldReturnGlobalStatistics() {
        when(workRepository.count()).thenReturn(10L);
        when(volumeRepository.countByOwnedTrue()).thenReturn(45L);
        when(userWorkRepository.countByStatus(WorkStatus.COMPLETED)).thenReturn(3L);
        when(userWorkRepository.countByStatus(WorkStatus.ONGOING)).thenReturn(5L);
        when(userWorkRepository.countByStatus(WorkStatus.WISHLIST)).thenReturn(2L);
        when(workRepository.countByType(WorkType.MANGA)).thenReturn(6L);
        when(workRepository.countByType(WorkType.COMIC)).thenReturn(2L);
        when(workRepository.countByType(WorkType.GRAPHIC_NOVEL)).thenReturn(1L);
        when(workRepository.countByType(WorkType.MANHWA)).thenReturn(1L);

        StatsResponseDTO stats = statsService.getStats();

        assertThat(stats).isNotNull();
        assertThat(stats.totalWorks()).isEqualTo(10L);
        assertThat(stats.totalVolumes()).isEqualTo(45L);
        assertThat(stats.completedWorks()).isEqualTo(3L);
        assertThat(stats.onGoingWorks()).isEqualTo(5L);
        assertThat(stats.wishlistItems()).isEqualTo(2L);
        assertThat(stats.totalMangas()).isEqualTo(6L);
        assertThat(stats.totalComics()).isEqualTo(2L);
        assertThat(stats.totalGraphicNovels()).isEqualTo(1L);
        assertThat(stats.totalManhwas()).isEqualTo(1L);

        verify(workRepository).count();
        verify(volumeRepository).countByOwnedTrue();
        verify(userWorkRepository).countByStatus(WorkStatus.COMPLETED);
        verify(userWorkRepository).countByStatus(WorkStatus.ONGOING);
        verify(userWorkRepository).countByStatus(WorkStatus.WISHLIST);
        verify(workRepository).countByType(WorkType.MANGA);
        verify(workRepository).countByType(WorkType.COMIC);
        verify(workRepository).countByType(WorkType.GRAPHIC_NOVEL);
        verify(workRepository).countByType(WorkType.MANHWA);
        verifyNoMoreInteractions(workRepository, volumeRepository, userWorkRepository);
    }

    @Test
    @DisplayName("Should return correct aggregated user-scoped statistics")
    void getUserStats_shouldReturnUserScopedStatistics() {
        when(userWorkRepository.countByUserId(1L)).thenReturn(4L);
        when(volumeRepository.countByUserIdAndOwnedTrue(1L)).thenReturn(20L);
        when(userWorkRepository.countByUserIdAndStatus(1L, WorkStatus.COMPLETED)).thenReturn(1L);
        when(userWorkRepository.countByUserIdAndStatus(1L, WorkStatus.ONGOING)).thenReturn(2L);
        when(userWorkRepository.countByUserIdAndStatus(1L, WorkStatus.WISHLIST)).thenReturn(1L);
        when(userWorkRepository.countByUserIdAndWorkType(1L, WorkType.MANGA)).thenReturn(3L);
        when(userWorkRepository.countByUserIdAndWorkType(1L, WorkType.COMIC)).thenReturn(1L);
        when(userWorkRepository.countByUserIdAndWorkType(1L, WorkType.GRAPHIC_NOVEL)).thenReturn(0L);
        when(userWorkRepository.countByUserIdAndWorkType(1L, WorkType.MANHWA)).thenReturn(0L);

        StatsResponseDTO stats = statsService.getUserStats(user);

        assertThat(stats).isNotNull();
        assertThat(stats.totalWorks()).isEqualTo(4L);
        assertThat(stats.totalVolumes()).isEqualTo(20L);
        assertThat(stats.completedWorks()).isEqualTo(1L);
        assertThat(stats.onGoingWorks()).isEqualTo(2L);
        assertThat(stats.wishlistItems()).isEqualTo(1L);
        assertThat(stats.totalMangas()).isEqualTo(3L);
        assertThat(stats.totalComics()).isEqualTo(1L);
        assertThat(stats.totalGraphicNovels()).isEqualTo(0L);
        assertThat(stats.totalManhwas()).isEqualTo(0L);

        verify(userWorkRepository).countByUserId(1L);
        verify(volumeRepository).countByUserIdAndOwnedTrue(1L);
        verify(userWorkRepository).countByUserIdAndStatus(1L, WorkStatus.COMPLETED);
        verify(userWorkRepository).countByUserIdAndStatus(1L, WorkStatus.ONGOING);
        verify(userWorkRepository).countByUserIdAndStatus(1L, WorkStatus.WISHLIST);
        verify(userWorkRepository).countByUserIdAndWorkType(1L, WorkType.MANGA);
        verify(userWorkRepository).countByUserIdAndWorkType(1L, WorkType.COMIC);
        verify(userWorkRepository).countByUserIdAndWorkType(1L, WorkType.GRAPHIC_NOVEL);
        verify(userWorkRepository).countByUserIdAndWorkType(1L, WorkType.MANHWA);
        verifyNoMoreInteractions(workRepository, volumeRepository, userWorkRepository);
    }
}
