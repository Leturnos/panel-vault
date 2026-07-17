package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.UserWork;
import io.github.leturnos.panelvault.model.WorkStatus;
import io.github.leturnos.panelvault.model.WorkType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserWorkRepository extends JpaRepository<UserWork, Long> {

    long countByStatus(WorkStatus status);

    Optional<UserWork> findByUserIdAndWorkId(Long userId, Long workId);

    long countByUserId(Long userId);

    long countByUserIdAndWorkType(Long userId, WorkType type);

    long countByUserIdAndStatus(Long userId, WorkStatus status);
}
