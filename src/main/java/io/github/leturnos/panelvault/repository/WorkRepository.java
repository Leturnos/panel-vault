package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.Work;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface WorkRepository extends JpaRepository<Work, Long> {

    boolean existsByTitle(String title);
}
