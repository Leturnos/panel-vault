package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.AbstractIntegrationTest;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.Volume;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class VolumeRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private VolumeRepository volumeRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    private User user1;
    private User user2;
    private Work work1;
    private Work work2;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("reader1");
        user1.setEmail("reader1@example.com");
        user1.setPassword("secret123");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("reader2");
        user2.setEmail("reader2@example.com");
        user2.setPassword("secret123");
        user2 = userRepository.save(user2);

        work1 = new Work();
        work1.setTitle("Hunter x Hunter");
        work1.setType(WorkType.MANGA);
        work1.setPublisher("JBC");
        work1.setAuthor("Yoshihiro Togashi");
        work1 = workRepository.save(work1);

        work2 = new Work();
        work2.setTitle("Kingdom");
        work2.setType(WorkType.MANGA);
        work2.setPublisher("Young Jump");
        work2.setAuthor("Yasuhisa Hara");
        work2 = workRepository.save(work2);
    }

    @Test
    @DisplayName("Should find volumes by work ID and user ID")
    void findByWorkIdAndUserId_shouldReturnMatchingVolumes() {
        Volume vol1 = new Volume();
        vol1.setNumber(1);
        vol1.setPurchaseDate(LocalDate.now());
        vol1.setPurchasePrice(new BigDecimal("35.00"));
        vol1.setOwned(true);
        vol1.setUser(user1);
        vol1.setWork(work1);
        volumeRepository.save(vol1);

        Volume vol2 = new Volume();
        vol2.setNumber(2);
        vol2.setPurchaseDate(LocalDate.now());
        vol2.setPurchasePrice(new BigDecimal("35.00"));
        vol2.setOwned(true);
        vol2.setUser(user1);
        vol2.setWork(work1);
        volumeRepository.save(vol2);

        // Volume for user2 with same work
        Volume volUser2 = new Volume();
        volUser2.setNumber(1);
        volUser2.setPurchaseDate(LocalDate.now());
        volUser2.setPurchasePrice(new BigDecimal("35.00"));
        volUser2.setOwned(true);
        volUser2.setUser(user2);
        volUser2.setWork(work1);
        volumeRepository.save(volUser2);

        List<Volume> results = volumeRepository.findByWorkIdAndUserId(work1.getId(), user1.getId());

        assertThat(results).hasSize(2);
        assertThat(results).allMatch(v -> v.getUser().getId().equals(user1.getId()));
        assertThat(results).allMatch(v -> v.getWork().getId().equals(work1.getId()));
    }

    @Test
    @DisplayName("Should count all global owned volumes")
    void countByOwnedTrue_shouldReturnTotalOwnedVolumesAcrossAllUsers() {
        Volume v1 = new Volume();
        v1.setNumber(1);
        v1.setOwned(true);
        v1.setUser(user1);
        v1.setWork(work1);
        volumeRepository.save(v1);

        Volume v2 = new Volume();
        v2.setNumber(2);
        v2.setOwned(false);
        v2.setUser(user1);
        v2.setWork(work1);
        volumeRepository.save(v2);

        Volume v3 = new Volume();
        v3.setNumber(1);
        v3.setOwned(true);
        v3.setUser(user2);
        v3.setWork(work2);
        volumeRepository.save(v3);

        long ownedCount = volumeRepository.countByOwnedTrue();

        assertThat(ownedCount).isEqualTo(2L);
    }

    @Test
    @DisplayName("Should count owned volumes for a specific user")
    void countByUserIdAndOwnedTrue_shouldReturnUserSpecificOwnedVolumes() {
        Volume v1 = new Volume();
        v1.setNumber(1);
        v1.setOwned(true);
        v1.setUser(user1);
        v1.setWork(work1);
        volumeRepository.save(v1);

        Volume v2 = new Volume();
        v2.setNumber(2);
        v2.setOwned(true);
        v2.setUser(user1);
        v2.setWork(work1);
        volumeRepository.save(v2);

        Volume v3 = new Volume();
        v3.setNumber(1);
        v3.setOwned(true);
        v3.setUser(user2);
        v3.setWork(work1);
        volumeRepository.save(v3);

        long user1Owned = volumeRepository.countByUserIdAndOwnedTrue(user1.getId());
        long user2Owned = volumeRepository.countByUserIdAndOwnedTrue(user2.getId());

        assertThat(user1Owned).isEqualTo(2L);
        assertThat(user2Owned).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should aggregate financial metrics for user and work")
    void financialMetrics_shouldAggregateCorrectly() {
        Volume v1 = new Volume();
        v1.setNumber(1);
        v1.setOwned(true);
        v1.setPurchaseDate(LocalDate.of(2026, 1, 15));
        v1.setPurchasePrice(new BigDecimal("30.00"));
        v1.setUser(user1);
        v1.setWork(work1);
        volumeRepository.save(v1);

        Volume v2 = new Volume();
        v2.setNumber(2);
        v2.setOwned(true);
        v2.setPurchaseDate(LocalDate.of(2026, 1, 20));
        v2.setPurchasePrice(new BigDecimal("50.00"));
        v2.setUser(user1);
        v2.setWork(work1);
        volumeRepository.save(v2);

        Volume v3 = new Volume();
        v3.setNumber(1);
        v3.setOwned(true);
        v3.setPurchaseDate(LocalDate.of(2026, 2, 10));
        v3.setPurchasePrice(new BigDecimal("40.00"));
        v3.setUser(user1);
        v3.setWork(work2);
        volumeRepository.save(v3);

        // Volume with owned = false should be excluded
        Volume vUnowned = new Volume();
        vUnowned.setNumber(3);
        vUnowned.setOwned(false);
        vUnowned.setPurchaseDate(LocalDate.of(2026, 1, 25));
        vUnowned.setPurchasePrice(new BigDecimal("100.00"));
        vUnowned.setUser(user1);
        vUnowned.setWork(work1);
        volumeRepository.save(vUnowned);

        // User 1 totals
        assertThat(volumeRepository.totalSpentByUser(user1.getId())).isEqualByComparingTo(new BigDecimal("120.00"));
        assertThat(volumeRepository.totalVolumesWithPrice(user1.getId())).isEqualTo(3);
        assertThat(volumeRepository.averageVolumePrice(user1.getId())).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(volumeRepository.highestPrice(user1.getId())).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(volumeRepository.lowestPrice(user1.getId())).isEqualByComparingTo(new BigDecimal("30.00"));

        // User 1 + Work 1 metrics
        assertThat(volumeRepository.totalSpentByUserAndWork(user1.getId(), work1.getId())).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(volumeRepository.averageVolumePriceByWork(user1.getId(), work1.getId())).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(volumeRepository.countByWorkIdAndUserIdAndOwnedTrue(work1.getId(), user1.getId())).isEqualTo(2L);

        // Monthly expenses between dates
        var expensesBetween = volumeRepository.findMonthlyExpensesBetween(user1.getId(), LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31));
        assertThat(expensesBetween).hasSize(1);
        assertThat(expensesBetween.getFirst().getYear()).isEqualTo(2026);
        assertThat(expensesBetween.getFirst().getMonth()).isEqualTo(1);
        assertThat(expensesBetween.getFirst().getTotalSpent()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(expensesBetween.getFirst().getVolumeCount()).isEqualTo(2L);

        // All monthly expenses
        var allExpenses = volumeRepository.findAllMonthlyExpenses(user1.getId());
        assertThat(allExpenses).hasSize(2);
        assertThat(allExpenses.getFirst().getYear()).isEqualTo(2026);
        assertThat(allExpenses.getFirst().getMonth()).isEqualTo(2);
        assertThat(allExpenses.getFirst().getTotalSpent()).isEqualByComparingTo(new BigDecimal("40.00"));
        assertThat(allExpenses.getFirst().getVolumeCount()).isEqualTo(1L);

        assertThat(allExpenses.getLast().getYear()).isEqualTo(2026);
        assertThat(allExpenses.getLast().getMonth()).isEqualTo(1);
        assertThat(allExpenses.getLast().getTotalSpent()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(allExpenses.getLast().getVolumeCount()).isEqualTo(2L);
    }
}
