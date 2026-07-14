package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.Work;
import io.github.leturnos.panelvault.model.WorkType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {

    boolean existsByTitle(String title);

    Page<Work> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    long countByType(WorkType type);
}
