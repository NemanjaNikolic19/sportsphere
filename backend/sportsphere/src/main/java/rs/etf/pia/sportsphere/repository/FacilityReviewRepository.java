package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.FacilityReview;

public interface FacilityReviewRepository
        extends JpaRepository<FacilityReview, Long> {

    List<FacilityReview>
        findTop5ByFacilityIdOrderByCreatedAtDesc(
            Long facilityId
        );

    long countByAthleteIdAndFacilityId(
        Long athleteId,
        Long facilityId
    );

    long countByFacilityIdAndReaction(
        Long facilityId,
        String reaction
    );
}