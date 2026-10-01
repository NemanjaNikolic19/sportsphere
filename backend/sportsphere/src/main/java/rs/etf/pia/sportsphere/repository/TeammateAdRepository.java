package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.TeammateAd;

public interface TeammateAdRepository extends JpaRepository<TeammateAd, Long> {
    
    List<TeammateAd> findByStatusOrderByPlayDateAscStartTimeAsc(String status);

    List<TeammateAd> findByAthleteIdOrderByCreatedAtDesc(Long athleteId);
}
