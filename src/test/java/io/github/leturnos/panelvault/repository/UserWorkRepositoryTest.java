package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.AbstractIntegrationTest;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@Transactional
class UserWorkRepositoryTest extends AbstractIntegrationTest {

    @Autowired
    private UserWorkRepository userWorkRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WorkRepository workRepository;

    private User user1;
    private User user2;
    private Work mangaWork;
    private Work comicWork;

    @BeforeEach
    void setUp() {
        user1 = new User();
        user1.setUsername("user1");
        user1.setEmail("user1@example.com");
        user1.setPassword("pass12345");
        user1 = userRepository.save(user1);

        user2 = new User();
        user2.setUsername("user2");
        user2.setEmail("user2@example.com");
        user2.setPassword("pass12345");
        user2 = userRepository.save(user2);

        mangaWork = new Work();
        mangaWork.setTitle("Chainsaw Man");
        mangaWork.setType(WorkType.MANGA);
        mangaWork.setPublisher("Panini");
        mangaWork.setAuthor("Tatsuki Fujimoto");
        mangaWork = workRepository.save(mangaWork);

        comicWork = new Work();
        comicWork.setTitle("Batman: Year One");
        comicWork.setType(WorkType.COMIC);
        comicWork.setPublisher("DC Comics");
        comicWork.setAuthor("Frank Miller");
        comicWork = workRepository.save(comicWork);
    }

    @Test
    @DisplayName("Should find UserWork by user ID and work ID")
    void findByUserIdAndWorkId_shouldReturnUserWork_whenExists() {
        UserWork userWork = new UserWork();
        userWork.setUser(user1);
        userWork.setWork(mangaWork);
        userWork.setStatus(WorkStatus.ONGOING);
        userWork.setRating(new BigDecimal("9.0"));
        userWorkRepository.save(userWork);

        Optional<UserWork> found = userWorkRepository.findByUserIdAndWorkId(user1.getId(), mangaWork.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getStatus()).isEqualTo(WorkStatus.ONGOING);
        assertThat(found.get().getRating()).isEqualByComparingTo("9.0");
    }

    @Test
    @DisplayName("Should count global UserWork entries by status")
    void countByStatus_shouldReturnCorrectCount() {
        UserWork uw1 = new UserWork();
        uw1.setUser(user1);
        uw1.setWork(mangaWork);
        uw1.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw1);

        UserWork uw2 = new UserWork();
        uw2.setUser(user2);
        uw2.setWork(comicWork);
        uw2.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw2);

        UserWork uw3 = new UserWork();
        uw3.setUser(user1);
        uw3.setWork(comicWork);
        uw3.setStatus(WorkStatus.ONGOING);
        userWorkRepository.save(uw3);

        long completedCount = userWorkRepository.countByStatus(WorkStatus.COMPLETED);
        long ongoingCount = userWorkRepository.countByStatus(WorkStatus.ONGOING);

        assertThat(completedCount).isEqualTo(2L);
        assertThat(ongoingCount).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should count works in a specific user's collection")
    void countByUserId_shouldReturnCorrectCount() {
        UserWork uw1 = new UserWork();
        uw1.setUser(user1);
        uw1.setWork(mangaWork);
        uw1.setStatus(WorkStatus.ONGOING);
        userWorkRepository.save(uw1);

        UserWork uw2 = new UserWork();
        uw2.setUser(user1);
        uw2.setWork(comicWork);
        uw2.setStatus(WorkStatus.WISHLIST);
        userWorkRepository.save(uw2);

        UserWork uw3 = new UserWork();
        uw3.setUser(user2);
        uw3.setWork(mangaWork);
        uw3.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw3);

        long user1Count = userWorkRepository.countByUserId(user1.getId());
        long user2Count = userWorkRepository.countByUserId(user2.getId());

        assertThat(user1Count).isEqualTo(2L);
        assertThat(user2Count).isEqualTo(1L);
    }

    @Test
    @DisplayName("Should count works by user ID and work type")
    void countByUserIdAndWorkType_shouldReturnCorrectCount() {
        UserWork uw1 = new UserWork();
        uw1.setUser(user1);
        uw1.setWork(mangaWork);
        uw1.setStatus(WorkStatus.ONGOING);
        userWorkRepository.save(uw1);

        UserWork uw2 = new UserWork();
        uw2.setUser(user1);
        uw2.setWork(comicWork);
        uw2.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw2);

        long mangaCount = userWorkRepository.countByUserIdAndWorkType(user1.getId(), WorkType.MANGA);
        long comicCount = userWorkRepository.countByUserIdAndWorkType(user1.getId(), WorkType.COMIC);
        long manhwaCount = userWorkRepository.countByUserIdAndWorkType(user1.getId(), WorkType.MANHWA);

        assertThat(mangaCount).isEqualTo(1L);
        assertThat(comicCount).isEqualTo(1L);
        assertThat(manhwaCount).isEqualTo(0L);
    }

    @Test
    @DisplayName("Should count works by user ID and status")
    void countByUserIdAndStatus_shouldReturnCorrectCount() {
        UserWork uw1 = new UserWork();
        uw1.setUser(user1);
        uw1.setWork(mangaWork);
        uw1.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw1);

        UserWork uw2 = new UserWork();
        uw2.setUser(user1);
        uw2.setWork(comicWork);
        uw2.setStatus(WorkStatus.COMPLETED);
        userWorkRepository.save(uw2);

        long completedCount = userWorkRepository.countByUserIdAndStatus(user1.getId(), WorkStatus.COMPLETED);
        long wishlistCount = userWorkRepository.countByUserIdAndStatus(user1.getId(), WorkStatus.WISHLIST);

        assertThat(completedCount).isEqualTo(2L);
        assertThat(wishlistCount).isEqualTo(0L);
    }
}
