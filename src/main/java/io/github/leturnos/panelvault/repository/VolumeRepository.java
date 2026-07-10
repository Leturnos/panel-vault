package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {
}
