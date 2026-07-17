package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {

    long countByOwnedTrue();

    List<Volume> findByWorkIdAndUserId(Long workId, Long userId);

    long countByUserIdAndOwnedTrue(Long userId);
}
