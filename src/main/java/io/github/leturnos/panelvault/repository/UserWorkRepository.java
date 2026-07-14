package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.WorkStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserWorkRepository extends JpaRepository<UserWork, Long> {

    long countByStatus(WorkStatus status);
}
