package rs.etf.pia.sportsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.TrainingSession;

import java.time.LocalDateTime;
import java.util.List;



public interface TrainingSessionRepository extends JpaRepository<TrainingSession, Long> {
    
    List<TrainingSession> findByAthleteIdOrderByStartTimeDesc(Long athleteId);

    List<TrainingSession> findByFacilityIdOrderByStartTimeDesc(Long facilityId);

    List<TrainingSession> findByResourceIdOrderByStartTimeAsc(Long resourceId);

    @Query("""
            SELECT ts
            FROM TrainingSession ts
            WHERE ts.trainerId = :trainerId
                AND ts.status <> 'CANCELLED'
                AND ts.startTime < :endTime
                AND ts.endTime > :startTime
            """)
    List<TrainingSession> findOverlappingTrainerSessions(
        @Param("trainerId") Long trainerId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
            SELECT ts
            FROM TrainingSession ts
            WHERE ts.resourceId = :resourceId
                AND ts.status <> 'CANCELLED'
                AND ts.startTime < :endTime
                AND ts.endTime > :startTime
            """)
    List<TrainingSession> findOverlappingResourceSessions(
        @Param("resourceId") Long resourceId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
            SELECT ts
            FROM TrainingSession ts
            WHERE ts.resourceId = :resourceId
                AND ts.id <> :sessionId
                AND ts.status <> 'CANCELLED'
                AND ts.startTime < :endTime
                AND ts.endTime > :startTime
            """)
    List<TrainingSession> findOverlappingResourceSessionsExcluding(
        @Param("resourceId") Long resourceId,
        @Param("sessionId") Long sessionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );

    @Query("""
            SELECT ts
            FROM TrainingSession ts
            WHERE ts.trainerId = :trainerId
                AND ts.id <> :sessionId
                AND ts.status <> 'CANCELLED'
                AND ts.startTime < :endTime
                AND ts.endTime > :startTime
            """)
    List<TrainingSession> findOverlappingTrainerSessionsExcluding(
        @Param("trainerId") Long trainerId,
        @Param("sessionId") Long sessionId,
        @Param("startTime") LocalDateTime startTime,
        @Param("endTime") LocalDateTime endTime
    );
}
