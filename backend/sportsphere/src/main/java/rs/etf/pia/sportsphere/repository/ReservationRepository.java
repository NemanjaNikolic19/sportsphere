package rs.etf.pia.sportsphere.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
    List<Reservation> findByAthleteIdOrderByStartTimeDesc(Long athleteId);

    List<Reservation> findByFacilityIdOrderByStartTimeDesc(Long facilityId);

    List<Reservation> findByFacilityIdAndStatusOrderByStartTimeAsc(Long facilityId, String status);

    List<Reservation> findByResourceIdOrderByStartTimeAsc(Long resourceId);

    @Query( """
            SELECT r
            FROM Reservation r
            WHERE r.resourceId = :resourceId
                AND r.status <> 'CANCELLED'
                AND r.startTime < :endTime
                AND r.endTime > :startTime
            """)
    List<Reservation> findOverlappingReservations(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.resourceId = :resourceId
                AND r.id <> :reservationId
                AND r.status <> 'CANCELLED'
                AND r.startTime < :endTime
                AND r.endTime > :startTime
            """)
    List<Reservation> findOverlappingReservationsExcluding(
        @Param("resourceId") Long resourceId,
        @Param("reservationId") Long reservationId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.facilityId IN :facilityIds
                AND r.status <> 'CANCELLED'
                AND r.startTime < :periodEnd
                AND r.endTime > :periodStart
            ORDER BY r.startTime ASC
            """)
    List<Reservation> findMonthlyReportReservations(
        @Param("facilityIds") List<Long> facilityIds,
        @Param("periodStart") LocalDateTime periodStart,
        @Param("periodEnd") LocalDateTime periodEnd
    );

    long countByAthleteIdAndFacilityIdAndStatus(
        Long athleteId,
        Long facilityId,
        String status
    );
}
