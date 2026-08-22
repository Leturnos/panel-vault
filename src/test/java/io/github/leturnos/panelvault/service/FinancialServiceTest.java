package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.FinancialSummaryResponseDTO;
import io.github.leturnos.panelvault.dto.MonthlyExpenseProjection;
import io.github.leturnos.panelvault.dto.MonthlyExpenseResponseDTO;
import io.github.leturnos.panelvault.dto.WorkFinancialResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialServiceTest {

    @Mock
    private WorkRepository workRepository;

    @Mock
    private VolumeRepository volumeRepository;

    @Mock
    private UserWorkRepository userWorkRepository;

    @InjectMocks
    private FinancialService financialService;

    private User user;
    private Work work1;
    private Work work2;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("collector");

        work1 = new Work();
        work1.setId(10L);
        work1.setTitle("Berserk");
        work1.setTotalVolumes(41);

        work2 = new Work();
        work2.setId(20L);
        work2.setTitle("One Piece");
        work2.setTotalVolumes(null); // Ongoing without defined total
    }

    @Nested
    @DisplayName("getSummary tests")
    class GetSummaryTests {

        @Test
        @DisplayName("Should return complete summary with correct estimation for ongoing works")
        void getSummary_shouldReturnCompleteSummary() {
            UserWork uw1 = new UserWork(1L, user, work1, WorkStatus.ONGOING, new BigDecimal("9.5"));
            UserWork uw2 = new UserWork(2L, user, work2, WorkStatus.ONGOING, new BigDecimal("10.0"));

            when(volumeRepository.totalSpentByUser(1L)).thenReturn(new BigDecimal("350.00"));
            when(volumeRepository.totalVolumesWithPrice(1L)).thenReturn(10);
            when(volumeRepository.averageVolumePrice(1L)).thenReturn(new BigDecimal("35.00"));
            when(volumeRepository.highestPrice(1L)).thenReturn(new BigDecimal("45.00"));
            when(volumeRepository.lowestPrice(1L)).thenReturn(new BigDecimal("25.00"));
            when(userWorkRepository.findByUserIdAndStatus(1L, WorkStatus.ONGOING)).thenReturn(List.of(uw1, uw2));
            when(volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(10L, 1L)).thenReturn(10L);

            FinancialSummaryResponseDTO summary = financialService.getSummary(user);

            assertThat(summary).isNotNull();
            assertThat(summary.totalSpent()).isEqualByComparingTo(new BigDecimal("350.00"));
            assertThat(summary.totalVolumesWithPrice()).isEqualTo(10);
            assertThat(summary.averageVolumePrice()).isEqualByComparingTo(new BigDecimal("35.00"));
            assertThat(summary.highestPrice()).isEqualByComparingTo(new BigDecimal("45.00"));
            assertThat(summary.lowestPrice()).isEqualByComparingTo(new BigDecimal("25.00"));
            // Missing volumes: Berserk 41 - 10 = 31. Estimated: 31 * 35.00 = 1085.00
            assertThat(summary.estimatedToCompleteCollection()).isEqualByComparingTo(new BigDecimal("1085.00"));
        }

        @Test
        @DisplayName("Should return zero values when user has no purchases or prices registered")
        void getSummary_shouldHandleNullsGracefully() {
            when(volumeRepository.totalSpentByUser(1L)).thenReturn(null);
            when(volumeRepository.totalVolumesWithPrice(1L)).thenReturn(null);
            when(volumeRepository.averageVolumePrice(1L)).thenReturn(null);
            when(volumeRepository.highestPrice(1L)).thenReturn(null);
            when(volumeRepository.lowestPrice(1L)).thenReturn(null);
            when(userWorkRepository.findByUserIdAndStatus(1L, WorkStatus.ONGOING)).thenReturn(List.of());

            FinancialSummaryResponseDTO summary = financialService.getSummary(user);

            assertThat(summary).isNotNull();
            assertThat(summary.totalSpent()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.totalVolumesWithPrice()).isEqualTo(0);
            assertThat(summary.averageVolumePrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.highestPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.lowestPrice()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(summary.estimatedToCompleteCollection()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("getSummaryByDate tests")
    class GetSummaryByDateTests {

        @Test
        @DisplayName("Should query monthly expenses with normalized dates when year is provided")
        void getSummaryByDate_shouldNormalizeDatesWhenYearProvided() {
            MonthlyExpenseProjection proj = mock(MonthlyExpenseProjection.class);
            when(proj.getYear()).thenReturn(2026);
            when(proj.getMonth()).thenReturn(5);
            when(proj.getTotalSpent()).thenReturn(new BigDecimal("120.500"));
            when(proj.getVolumeCount()).thenReturn(3L);

            LocalDate expectedStart = LocalDate.of(2026, 1, 1);
            LocalDate expectedEnd = LocalDate.of(2026, 12, 31);

            when(volumeRepository.findMonthlyExpensesBetween(1L, expectedStart, expectedEnd))
                    .thenReturn(List.of(proj));

            List<MonthlyExpenseResponseDTO> result = financialService.getSummaryByDate(user, 2026, null, null);

            assertThat(result).hasSize(1);
            assertThat(result.getFirst().year()).isEqualTo(2026);
            assertThat(result.getFirst().month()).isEqualTo(5);
            assertThat(result.getFirst().totalSpent()).isEqualByComparingTo(new BigDecimal("120.50"));
            assertThat(result.getFirst().volumeCount()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Should query monthly expenses with provided start and end dates")
        void getSummaryByDate_shouldUseExplicitDatesWhenProvided() {
            LocalDate start = LocalDate.of(2026, 3, 1);
            LocalDate end = LocalDate.of(2026, 6, 30);

            when(volumeRepository.findMonthlyExpensesBetween(1L, start, end))
                    .thenReturn(List.of());

            List<MonthlyExpenseResponseDTO> result = financialService.getSummaryByDate(user, null, start, end);

            assertThat(result).isEmpty();
            verify(volumeRepository).findMonthlyExpensesBetween(1L, start, end);
        }

        @Test
        @DisplayName("Should query all monthly expenses when no filter is provided")
        void getSummaryByDate_shouldFindAll_whenNoFilterProvided() {
            when(volumeRepository.findAllMonthlyExpenses(1L)).thenReturn(List.of());

            List<MonthlyExpenseResponseDTO> result = financialService.getSummaryByDate(user, null, null, null);

            assertThat(result).isEmpty();
            verify(volumeRepository).findAllMonthlyExpenses(1L);
        }
    }

    @Nested
    @DisplayName("getSummaryByWork tests")
    class GetSummaryByWorkTests {

        @Test
        @DisplayName("Should return work financial details when work exists")
        void getSummaryByWork_shouldReturnWorkFinancialDetails() {
            when(workRepository.findById(10L)).thenReturn(Optional.of(work1));
            when(volumeRepository.totalSpentByUserAndWork(1L, 10L)).thenReturn(new BigDecimal("350.00"));
            when(volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(10L, 1L)).thenReturn(10L);
            when(volumeRepository.averageVolumePriceByWork(1L, 10L)).thenReturn(new BigDecimal("35.00"));

            WorkFinancialResponseDTO dto = financialService.getSummaryByWork(user, 10L);

            assertThat(dto).isNotNull();
            assertThat(dto.workId()).isEqualTo(10L);
            assertThat(dto.workTitle()).isEqualTo("Berserk");
            assertThat(dto.totalSpent()).isEqualByComparingTo(new BigDecimal("350.00"));
            assertThat(dto.ownedVolumesCount()).isEqualTo(10);
            assertThat(dto.totalVolumes()).isEqualTo(41);
            assertThat(dto.averagePricePaid()).isEqualByComparingTo(new BigDecimal("35.00"));
            // Missing: 41 - 10 = 31 * 35.00 = 1085.00
            assertThat(dto.estimatedRemainingCost()).isEqualByComparingTo(new BigDecimal("1085.00"));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work does not exist")
        void getSummaryByWork_shouldThrowException_whenWorkNotFound() {
            when(workRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> financialService.getSummaryByWork(user, 999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");
        }
    }

    @Nested
    @DisplayName("getSummaryByUser tests")
    class GetSummaryByUserTests {

        @Test
        @DisplayName("Should return paginated work financial summaries")
        void getSummaryByUser_shouldReturnPaginatedResponse() {
            UserWork uw = new UserWork(1L, user, work1, WorkStatus.ONGOING, new BigDecimal("9.5"));
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserWork> userWorkPage = new PageImpl<>(List.of(uw), pageable, 1);

            when(userWorkRepository.findByUserId(1L, pageable)).thenReturn(userWorkPage);
            when(volumeRepository.totalSpentByUserAndWork(1L, 10L)).thenReturn(new BigDecimal("350.00"));
            when(volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(10L, 1L)).thenReturn(10L);
            when(volumeRepository.averageVolumePriceByWork(1L, 10L)).thenReturn(new BigDecimal("35.00"));

            Page<WorkFinancialResponseDTO> result = financialService.getSummaryByUser(user, pageable);

            assertThat(result).isNotNull();
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().workId()).isEqualTo(10L);
            assertThat(result.getContent().getFirst().workTitle()).isEqualTo("Berserk");
            assertThat(result.getContent().getFirst().totalSpent()).isEqualByComparingTo(new BigDecimal("350.00"));
        }
    }
}
