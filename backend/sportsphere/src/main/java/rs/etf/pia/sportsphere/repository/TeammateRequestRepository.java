package rs.etf.pia.sportsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.TeammateRequest;
import java.util.Optional;
import java.util.List;




public interface TeammateRequestRepository extends JpaRepository<TeammateRequest, Long> {
    
    boolean existsByAdIdAndAthleteId(Long adId, Long athleteId);

    Optional<TeammateRequest> findByAdIdAndAthleteId(Long adId, Long athleteId);

    List<TeammateRequest> findByAdId(Long adId);

    @Query("""
            SELECT tr
            FROM TeammateRequest tr
            WHERE tr.adId IN (
                SELECT ta.id
                FROM TeammateAd ta
                WHERE ta.athleteId = :ownerId
            )
            ORDER BY tr.createdAt DESC
            """)
    List<TeammateRequest> findRequestsForOwnerAds(@Param("ownerId") Long ownerId);
}
