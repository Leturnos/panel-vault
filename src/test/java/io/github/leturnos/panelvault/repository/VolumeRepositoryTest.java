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
}
