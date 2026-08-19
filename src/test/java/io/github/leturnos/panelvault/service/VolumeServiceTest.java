package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.VolumeRequestDTO;
import io.github.leturnos.panelvault.dto.VolumeResponseDTO;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.User;
import io.github.leturnos.panelvault.model.Volume;
import io.github.leturnos.panelvault.model.Work;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VolumeServiceTest {

    @Mock
    private VolumeRepository repository;

    @Mock
    private WorkRepository workRepository;

    @InjectMocks
    private VolumeService volumeService;

    private User owner;
    private User otherUser;
    private Work work;

    @BeforeEach
    void setUp() {
        owner = new User();
        owner.setId(1L);
        owner.setUsername("owner");

        otherUser = new User();
        otherUser.setId(2L);
        otherUser.setUsername("other");

        work = new Work();
        work.setId(10L);
        work.setTitle("One Piece");
    }

    @Nested
    @DisplayName("Create volume tests")
    class CreateTests {

        @Test
        @DisplayName("Should create volume successfully when work exists")
        void create_shouldSaveAndReturnVolume_whenWorkExists() {
            VolumeRequestDTO request = new VolumeRequestDTO(1, LocalDate.of(2023, 1, 1), new BigDecimal("29.90"), true);

            when(workRepository.findById(10L)).thenReturn(Optional.of(work));
            when(repository.save(any(Volume.class))).thenAnswer(invocation -> {
                Volume v = invocation.getArgument(0);
                v.setId(100L);
                return v;
            });

            VolumeResponseDTO response = volumeService.create(10L, request, owner);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(100L);
            assertThat(response.number()).isEqualTo(1);
            assertThat(response.purchasePrice()).isEqualByComparingTo("29.90");
            assertThat(response.workId()).isEqualTo(10L);

            verify(repository).save(argThat(v -> v.getUser().equals(owner) && v.getWork().equals(work)));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work does not exist")
        void create_shouldThrowException_whenWorkDoesNotExist() {
            VolumeRequestDTO request = new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true);

            when(workRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> volumeService.create(999L, request, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find volumes tests")
    class FindTests {

        @Test
        @DisplayName("Should return all volumes belonging to the user for a given work")
        void findAllByWorkId_shouldReturnUserVolumes_whenWorkExists() {
            Volume vol1 = new Volume(new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true));
            vol1.setId(101L);
            vol1.setWork(work);
            vol1.setUser(owner);

            when(workRepository.existsById(10L)).thenReturn(true);
            when(repository.findByWorkIdAndUserId(10L, 1L)).thenReturn(List.of(vol1));

            List<VolumeResponseDTO> result = volumeService.findAllByWorkId(10L, owner);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).number()).isEqualTo(1);
            assertThat(result.get(0).workId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work does not exist during findAllByWorkId")
        void findAllByWorkId_shouldThrowException_whenWorkDoesNotExist() {
            when(workRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> volumeService.findAllByWorkId(999L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");
        }

        @Test
        @DisplayName("Should return volume by ID when it belongs to the authenticated user")
        void findById_shouldReturnVolume_whenBelongsToUser() {
            Volume volume = new Volume(new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true));
            volume.setId(200L);
            volume.setWork(work);
            volume.setUser(owner);

            when(repository.findById(200L)).thenReturn(Optional.of(volume));

            VolumeResponseDTO result = volumeService.findById(200L, owner);

            assertThat(result).isNotNull();
            assertThat(result.id()).isEqualTo(200L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when volume belongs to a different user (isolation)")
        void findById_shouldThrowException_whenBelongsToDifferentUser() {
            Volume volume = new Volume(new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true));
            volume.setId(200L);
            volume.setWork(work);
            volume.setUser(otherUser);

            when(repository.findById(200L)).thenReturn(Optional.of(volume));

            assertThatThrownBy(() -> volumeService.findById(200L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Volume não encontrado");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when volume ID is not found")
        void findById_shouldThrowException_whenVolumeNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> volumeService.findById(999L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Volume não encontrado");
        }
    }

    @Nested
    @DisplayName("Delete volume tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete volume when it belongs to the authenticated user")
        void delete_shouldRemoveVolume_whenBelongsToUser() {
            Volume volume = new Volume(new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true));
            volume.setId(300L);
            volume.setWork(work);
            volume.setUser(owner);

            when(repository.findById(300L)).thenReturn(Optional.of(volume));

            volumeService.delete(300L, owner);

            verify(repository).delete(volume);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when attempting to delete another user's volume")
        void delete_shouldThrowException_whenBelongsToDifferentUser() {
            Volume volume = new Volume(new VolumeRequestDTO(1, LocalDate.now(), BigDecimal.TEN, true));
            volume.setId(300L);
            volume.setWork(work);
            volume.setUser(otherUser);

            when(repository.findById(300L)).thenReturn(Optional.of(volume));

            assertThatThrownBy(() -> volumeService.delete(300L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Volume não encontrado");

            verify(repository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent volume")
        void delete_shouldThrowException_whenVolumeNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> volumeService.delete(999L, owner))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Volume não encontrado");

            verify(repository, never()).delete(any());
        }
    }
}
