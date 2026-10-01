package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.Trainer;

public interface TrainerRepository extends JpaRepository<Trainer, Long> {
    
    @Query("""
            SELECT t
            FROM Trainer t
            WHERE t.active = true
                AND (:facilityId IS NULL OR t.facilityId = :facilityId)
                AND (:sportId IS NULL OR t.sportId = :sportId)
            ORDER BY t.lastName, t.firstName
            """)
    List<Trainer> searchActiveTrainers (
        @Param("facilityId") Long facilityId,
        @Param("sportId") Long sportId
    );

    List<Trainer> findAllByOrderByLastNameAscFirstNameAsc();
}
