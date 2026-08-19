package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.UserWorkRequestDTO;
import io.github.leturnos.panelvault.dto.UserWorkResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.repository.UserWorkRepository;
import io.github.leturnos.panelvault.repository.WorkRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserWorkServiceTest {

    @Mock
    private UserWorkRepository userWorkRepository;

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private UserWorkService userWorkService;

    private User user;
    private Work work;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setUsername("collector");

        work = new Work();
        work.setId(10L);
        work.setTitle("Berserk");
    }

    @Nested
    @DisplayName("Save or update tests")
    class SaveOrUpdateTests {

        @Test
        @DisplayName("Should create new UserWork entry when not yet in user collection")
        void saveOrUpdate_shouldCreateNew_whenNotExistsInCollection() {
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.ONGOING, new BigDecimal("5.0"));

            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.empty());
            when(workRepository.findById(10L)).thenReturn(Optional.of(work));
            when(userWorkRepository.save(any(UserWork.class))).thenAnswer(invocation -> {
                UserWork uw = invocation.getArgument(0);
                uw.setId(50L);
                return uw;
            });

            UserWorkResponseDTO response = userWorkService.saveOrUpdate(10L, request, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(50L);
            assertThat(response.workId()).isEqualTo(10L);
            assertThat(response.workTitle()).isEqualTo("Berserk");
            assertThat(response.status()).isEqualTo(WorkStatus.ONGOING);
            assertThat(response.rating()).isEqualByComparingTo("5.0");

            verify(userWorkRepository).save(argThat(uw -> uw.getUser().equals(user) && uw.getWork().equals(work) && uw.getRating().compareTo(new BigDecimal("5.0")) == 0));
        }

        @Test
        @DisplayName("Should update existing UserWork entry when already in user collection")
        void saveOrUpdate_shouldUpdate_whenAlreadyExistsInCollection() {
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.COMPLETED, new BigDecimal("4.0"));

            UserWork existing = new UserWork();
            existing.setId(50L);
            existing.setUser(user);
            existing.setWork(work);
            existing.setStatus(WorkStatus.ONGOING);
            existing.setRating(new BigDecimal("3.0"));

            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.of(existing));
            when(userWorkRepository.save(any(UserWork.class))).thenAnswer(invocation -> invocation.getArgument(0));

            UserWorkResponseDTO response = userWorkService.saveOrUpdate(10L, request, user);

            assertThat(response).isNotNull();
            assertThat(response.status()).isEqualTo(WorkStatus.COMPLETED);
            assertThat(response.rating()).isEqualByComparingTo("4.0");

            verify(workRepository, never()).findById(any());
            verify(userWorkRepository).save(existing);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work does not exist")
        void saveOrUpdate_shouldThrowException_whenWorkDoesNotExist() {
            UserWorkRequestDTO request = new UserWorkRequestDTO(WorkStatus.WISHLIST, null);

            when(userWorkRepository.findByUserIdAndWorkId(1L, 999L)).thenReturn(Optional.empty());
            when(workRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userWorkService.saveOrUpdate(999L, request, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");

            verify(userWorkRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find user work tests")
    class FindTests {

        @Test
        @DisplayName("Should return user work details when present in collection")
        void findByWorkIdAndUser_shouldReturnDTO_whenPresent() {
            UserWork userWork = new UserWork();
            userWork.setId(50L);
            userWork.setUser(user);
            userWork.setWork(work);
            userWork.setStatus(WorkStatus.COMPLETED);
            userWork.setRating(new BigDecimal("5.0"));

            when(workRepository.existsById(10L)).thenReturn(true);
            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.of(userWork));

            UserWorkResponseDTO response = userWorkService.findByWorkIdAndUser(10L, user);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(50L);
            assertThat(response.workTitle()).isEqualTo("Berserk");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work does not exist")
        void findByWorkIdAndUser_shouldThrowException_whenWorkNotFound() {
            when(workRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userWorkService.findByWorkIdAndUser(999L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work exists but not in user's collection")
        void findByWorkIdAndUser_shouldThrowException_whenNotInCollection() {
            when(workRepository.existsById(10L)).thenReturn(true);
            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userWorkService.findByWorkIdAndUser(10L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não adicionada à coleção do usuário");
        }
    }

    @Nested
    @DisplayName("Delete user work tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete user work entry from collection when present")
        void delete_shouldRemove_whenPresent() {
            UserWork userWork = new UserWork();
            userWork.setId(50L);
            userWork.setUser(user);
            userWork.setWork(work);

            when(workRepository.existsById(10L)).thenReturn(true);
            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.of(userWork));

            userWorkService.delete(10L, user);

            verify(userWorkRepository).delete(userWork);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent work from collection")
        void delete_shouldThrowException_whenWorkNotFound() {
            when(workRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> userWorkService.delete(999L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");

            verify(userWorkRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting work that was never added to collection")
        void delete_shouldThrowException_whenNotInCollection() {
            when(workRepository.existsById(10L)).thenReturn(true);
            when(userWorkRepository.findByUserIdAndWorkId(1L, 10L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userWorkService.delete(10L, user))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não adicionada à coleção do usuário");

            verify(userWorkRepository, never()).delete(any());
        }
    }
}
