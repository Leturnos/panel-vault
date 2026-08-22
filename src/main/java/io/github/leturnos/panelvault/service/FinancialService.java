package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.FinancialSummaryResponseDTO;
import io.github.leturnos.panelvault.dto.MonthlyExpenseResponseDTO;
import io.github.leturnos.panelvault.dto.WorkFinancialResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.dto.MonthlyExpenseProjection;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.VolumeRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

@Service
public class FinancialService {

    private final WorkRepository workRepository;
    private final VolumeRepository volumeRepository;
    private final UserWorkRepository userWorkRepository;

    public FinancialService(
            WorkRepository workRepository,
            VolumeRepository volumeRepository,
            UserWorkRepository userWorkRepository) {
        this.workRepository = workRepository;
        this.volumeRepository = volumeRepository;
        this.userWorkRepository = userWorkRepository;
    }

    @Transactional(readOnly = true)
    public FinancialSummaryResponseDTO getSummary(User user) {
        BigDecimal totalSpent = volumeRepository.totalSpentByUser(user.getId());
        Integer totalVolumesWithPrice = volumeRepository.totalVolumesWithPrice(user.getId());
        BigDecimal averageVolumePrice = volumeRepository.averageVolumePrice(user.getId());
        BigDecimal highestPrice = volumeRepository.highestPrice(user.getId());
        BigDecimal lowestPrice = volumeRepository.lowestPrice(user.getId());

        if (averageVolumePrice != null) {
            averageVolumePrice = averageVolumePrice.setScale(2, RoundingMode.HALF_UP);
        }

        List<UserWork> ongoingWorks = userWorkRepository.findByUserIdAndStatus(user.getId(), WorkStatus.ONGOING);
        long missingVolumes = 0;
        for (UserWork userWork : ongoingWorks) {
            Integer totalVolumes = userWork.getWork().getTotalVolumes();
            if (totalVolumes != null && totalVolumes > 0) {
                long ownedCount = volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(userWork.getWork().getId(), user.getId());
                long missingForThisWork = Math.max(0, totalVolumes - ownedCount);
                missingVolumes += missingForThisWork;
            }
        }

        BigDecimal estimatedToCompleteCollection = (averageVolumePrice != null && missingVolumes > 0)
                ? averageVolumePrice.multiply(BigDecimal.valueOf(missingVolumes)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new FinancialSummaryResponseDTO(
                totalSpent != null ? totalSpent : BigDecimal.ZERO,
                totalVolumesWithPrice != null ? totalVolumesWithPrice : 0,
                averageVolumePrice != null ? averageVolumePrice : BigDecimal.ZERO,
                highestPrice != null ? highestPrice : BigDecimal.ZERO,
                lowestPrice != null ? lowestPrice : BigDecimal.ZERO,
                estimatedToCompleteCollection
        );
    }

    @Transactional(readOnly = true)
    public List<MonthlyExpenseResponseDTO> getSummaryByDate(
            User user,
            Integer year,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDate start = startDate;
        LocalDate end = endDate;

        if (start == null && end == null && year != null) {
            start = LocalDate.of(year, 1, 1);
            end = LocalDate.of(year, 12, 31);
        }

        List<MonthlyExpenseProjection> projections;
        if (start != null || end != null) {
            LocalDate effectiveStart = start != null ? start : LocalDate.of(1970, 1, 1);
            LocalDate effectiveEnd = end != null ? end : LocalDate.now();
            projections = volumeRepository.findMonthlyExpensesBetween(user.getId(), effectiveStart, effectiveEnd);
        } else {
            projections = volumeRepository.findAllMonthlyExpenses(user.getId());
        }

        return projections.stream()
                .map(p -> new MonthlyExpenseResponseDTO(
                        p.getYear(),
                        p.getMonth(),
                        p.getTotalSpent() != null ? p.getTotalSpent().setScale(2, RoundingMode.HALF_UP) : BigDecimal.ZERO,
                        p.getVolumeCount()
                ))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<WorkFinancialResponseDTO> getSummaryByUser(User user, Pageable pageable) {
        Page<UserWork> userWorks = userWorkRepository.findByUserId(user.
                getId(), pageable);
        return userWorks.map(uw -> buildWorkFinancialDTO(user, uw.getWork()));
    }

    @Transactional(readOnly = true)
    public WorkFinancialResponseDTO getSummaryByWork(User user, Long workId) {
        Work work = workRepository.findById(workId)
                .orElseThrow(() -> new ResourceNotFoundException("Obra não encontrada"));

        return buildWorkFinancialDTO(user, work);
    }

    private WorkFinancialResponseDTO buildWorkFinancialDTO(User user, Work work) {
        Long workId = work.getId();
        String workTitle = work.getTitle();
        BigDecimal totalSpent = volumeRepository.totalSpentByUserAndWork(user.getId(), workId);
        Integer ownedVolumesCount = Math.toIntExact(volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(workId, user.getId()));
        Integer totalVolumes = work.getTotalVolumes();

        int missing = 0;
        if (totalVolumes != null && totalVolumes > ownedVolumesCount) {
            missing = totalVolumes - ownedVolumesCount;
        }

        BigDecimal averagePricePaid = volumeRepository.averageVolumePriceByWork(user.getId(), workId);
        if (averagePricePaid != null) {
            averagePricePaid = averagePricePaid.setScale(2, RoundingMode.HALF_UP);
        }

        BigDecimal estimatedRemainingCost = (averagePricePaid != null && missing > 0)
                ? averagePricePaid.multiply(BigDecimal.valueOf(missing)).setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return new WorkFinancialResponseDTO(
                workId,
                workTitle,
                totalSpent != null ? totalSpent : BigDecimal.ZERO,
                ownedVolumesCount,
                totalVolumes,
                averagePricePaid != null ? averagePricePaid : BigDecimal.ZERO,
                estimatedRemainingCost
        );
    }
}
