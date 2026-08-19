package io.github.leturnos.panelvault.service;

import io.github.leturnos.panelvault.dto.WorkRequestDTO;
import io.github.leturnos.panelvault.dto.WorkResponseDTO;
import io.github.leturnos.panelvault.exception.DuplicateResourceException;
import io.github.leturnos.panelvault.exception.ResourceNotFoundException;
import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkType;
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

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkServiceTest {

    @Mock
    private WorkRepository repository;

    @InjectMocks
    private WorkService workService;

    private Work work;
    private WorkRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        requestDTO = new WorkRequestDTO(
                "Akira",
                WorkType.MANGA,
                "JBC",
                "Katsuhiro Otomo",
                6,
                "https://example.com/cover.jpg"
        );

        work = new Work(requestDTO);
        work.setId(1L);
    }

    @Nested
    @DisplayName("Create work tests")
    class CreateTests {

        @Test
        @DisplayName("Should create work successfully when title is unique")
        void create_shouldSaveAndReturnDTO_whenTitleIsUnique() {
            when(repository.existsByTitle("Akira")).thenReturn(false);
            when(repository.save(any(Work.class))).thenReturn(work);

            WorkResponseDTO response = workService.create(requestDTO);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Akira");
            assertThat(response.type()).isEqualTo(WorkType.MANGA);

            verify(repository).save(any(Work.class));
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when title already exists")
        void create_shouldThrowException_whenTitleAlreadyExists() {
            when(repository.existsByTitle("Akira")).thenReturn(true);

            assertThatThrownBy(() -> workService.create(requestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Já existe uma obra cadastrada com este título.");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Find work tests")
    class FindTests {

        @Test
        @DisplayName("Should return paged works without title filter")
        void findAll_shouldReturnPagedWorks_whenTitleFilterIsNull() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Work> page = new PageImpl<>(List.of(work), pageable, 1);

            when(repository.findAll(pageable)).thenReturn(page);

            Page<WorkResponseDTO> result = workService.findAll(null, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("Akira");
            verify(repository).findAll(pageable);
        }

        @Test
        @DisplayName("Should return filtered paged works when title filter is provided")
        void findAll_shouldReturnFilteredWorks_whenTitleFilterIsProvided() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Work> page = new PageImpl<>(List.of(work), pageable, 1);

            when(repository.findByTitleContainingIgnoreCase("akira", pageable)).thenReturn(page);

            Page<WorkResponseDTO> result = workService.findAll("akira", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).title()).isEqualTo("Akira");
            verify(repository).findByTitleContainingIgnoreCase("akira", pageable);
        }

        @Test
        @DisplayName("Should return work by ID when exists")
        void findById_shouldReturnDTO_whenWorkExists() {
            when(repository.findById(1L)).thenReturn(Optional.of(work));

            WorkResponseDTO response = workService.findById(1L);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("Akira");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when work ID does not exist")
        void findById_shouldThrowException_whenWorkDoesNotExist() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workService.findById(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");
        }
    }

    @Nested
    @DisplayName("Update work tests")
    class UpdateTests {

        @Test
        @DisplayName("Should update work successfully when exists and new title is unique")
        void update_shouldUpdateAndReturnDTO_whenValid() {
            WorkRequestDTO updateDTO = new WorkRequestDTO(
                    "Akira Deluxe",
                    WorkType.MANGA,
                    "JBC",
                    "Katsuhiro Otomo",
                    6,
                    "https://example.com/cover2.jpg"
            );

            when(repository.findById(1L)).thenReturn(Optional.of(work));
            when(repository.existsByTitle("Akira Deluxe")).thenReturn(false);
            when(repository.save(any(Work.class))).thenReturn(work);

            WorkResponseDTO response = workService.update(1L, updateDTO);

            assertThat(response).isNotNull();
            assertThat(response.title()).isEqualTo("Akira Deluxe");
            verify(repository).save(work);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when updating non-existent work")
        void update_shouldThrowException_whenWorkNotFound() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workService.update(999L, requestDTO))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw DuplicateResourceException when updated title already exists")
        void update_shouldThrowException_whenTitleAlreadyExists() {
            when(repository.findById(1L)).thenReturn(Optional.of(work));
            when(repository.existsByTitle("Akira")).thenReturn(true);

            assertThatThrownBy(() -> workService.update(1L, requestDTO))
                    .isInstanceOf(DuplicateResourceException.class)
                    .hasMessage("Já existe uma obra cadastrada com este título.");

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete work tests")
    class DeleteTests {

        @Test
        @DisplayName("Should delete work when work exists")
        void delete_shouldRemove_whenWorkExists() {
            when(repository.findById(1L)).thenReturn(Optional.of(work));

            workService.delete(1L);

            verify(repository).delete(work);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent work")
        void delete_shouldThrowException_whenWorkDoesNotExist() {
            when(repository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> workService.delete(999L))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessage("Obra não encontrada");

            verify(repository, never()).delete(any());
        }
    }
}
