package rs.etf.pia.sportsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.EquipmentOrder;

import java.time.LocalDateTime;
import java.util.List;


public interface EquipmentOrderRepository extends JpaRepository<EquipmentOrder, Long> {
    
    List<EquipmentOrder> findByAthleteIdOrderByCreatedAtDesc(Long athleteId);

    List<EquipmentOrder> findByFacilityIdOrderByCreatedAtDesc(Long facilityId);

    List<EquipmentOrder> findByFacilityIdInOrderByCreatedAtDesc(List<Long> facilityIds);

    @Query("""
            SELECT eo
            FROM EquipmentOrder eo
            WHERE eo.facilityId IN :facilityIds
                AND eo.status <> 'CANCELLED'
                AND eo.createdAt >= :periodStart
                AND eo.createdAt < :periodEnd
            ORDER BY eo.createdAt ASC
            """)
    List<EquipmentOrder> findMonthlyReportOrders(
        @Param("facilityIds") List<Long> facilityIds,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );
}
