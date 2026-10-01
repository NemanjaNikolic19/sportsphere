package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.FacilityImage;

public interface FacilityImageRepository extends JpaRepository<FacilityImage, Long> {
    
    List<FacilityImage> findByFacilityId(Long facilityId);
}
