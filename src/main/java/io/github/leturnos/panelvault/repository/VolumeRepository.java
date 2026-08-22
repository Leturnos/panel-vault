package io.github.leturnos.panelvault.repository;

import io.github.leturnos.panelvault.dto.MonthlyExpenseProjection;
import io.github.leturnos.panelvault.model.Volume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface VolumeRepository extends JpaRepository<Volume, Long> {

    long countByOwnedTrue();

    List<Volume> findByWorkIdAndUserId(Long workId, Long userId);

    long countByUserIdAndOwnedTrue(Long userId);

    long countByWorkIdAndUserIdAndOwnedTrue(Long workId, Long userId);

    @Query("""
            SELECT SUM(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
            """)
    BigDecimal totalSpentByUser(@Param("userId") Long userId);

    @Query("""
            SELECT SUM(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
              AND v.work.id = :workId
            """)
    BigDecimal totalSpentByUserAndWork(@Param("userId") Long userId, @Param("workId") Long workId);

    @Query("""
            SELECT EXTRACT(YEAR FROM v.purchaseDate)  AS year,
                   EXTRACT(MONTH FROM v.purchaseDate) AS month,
                   SUM(v.purchasePrice)               AS totalSpent,
                   COUNT(v.id)                        AS volumeCount
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchaseDate IS NOT NULL
              AND v.purchasePrice IS NOT NULL
            GROUP BY EXTRACT(YEAR FROM v.purchaseDate), EXTRACT(MONTH FROM v.purchaseDate)
            ORDER BY EXTRACT(YEAR FROM v.purchaseDate) DESC, EXTRACT(MONTH FROM v.purchaseDate) DESC
        """)
    List<MonthlyExpenseProjection> findAllMonthlyExpenses(@Param("userId") Long userId);

    @Query("""
            SELECT EXTRACT(YEAR FROM v.purchaseDate)  AS year,
                   EXTRACT(MONTH FROM v.purchaseDate) AS month,
                   SUM(v.purchasePrice)               AS totalSpent,
                   COUNT(v.id)                        AS volumeCount
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchaseDate IS NOT NULL
              AND v.purchasePrice IS NOT NULL
              AND v.purchaseDate >= :startDate
              AND v.purchaseDate <= :endDate
            GROUP BY EXTRACT(YEAR FROM v.purchaseDate), EXTRACT(MONTH FROM v.purchaseDate)
            ORDER BY EXTRACT(YEAR FROM v.purchaseDate) DESC, EXTRACT(MONTH FROM v.purchaseDate) DESC
        """)
    List<MonthlyExpenseProjection> findMonthlyExpensesBetween(
            @Param("userId") Long userId,
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate
    );

    @Query("""
            SELECT COUNT(v.id)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
            """)
    Integer totalVolumesWithPrice(@Param("userId") Long userId);

    @Query("""
            SELECT AVG(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
            """)
    BigDecimal averageVolumePrice(@Param("userId") Long userId);

    @Query("""
            SELECT AVG(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
              AND v.work.id = :workId
            """)
    BigDecimal averageVolumePriceByWork(@Param("userId") Long userId, @Param("workId") Long workId);

    @Query("""
            SELECT MAX(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
            """)
    BigDecimal highestPrice(@Param("userId") Long userId);

    @Query("""
            SELECT MIN(v.purchasePrice)
            FROM Volume v
            WHERE v.user.id = :userId
              AND v.owned = true
              AND v.purchasePrice IS NOT NULL
            """)
    BigDecimal lowestPrice(@Param("userId") Long userId);
}
