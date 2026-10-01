package rs.etf.pia.sportsphere.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.Promotion;

public interface PromotionRepository extends JpaRepository<Promotion, Long> {
    
    @Query("""
            SELECT p
            FROM Promotion p
            WHERE p.active = true
                AND p.validFrom <= :today
                AND p.validTo >= :today
            ORDER BY p.validTo ASC
            """)
    List<Promotion> findActivePromotions(@Param("today") LocalDate today);

    @Query("""
            SELECT p
            FROM Promotion p
            WHERE p.active = true
                AND p.validFrom > :today
            ORDER BY p.validFrom ASC
            """)
    List<Promotion> findUpcomingPromotions(@Param("today") LocalDate today);

    List<Promotion> findByFacilityIdInOrderByValidFromDesc(List<Long> facilityIds);
}
