package rs.etf.pia.sportsphere.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.Resource;
import java.util.List;


public interface ResourceRepository extends JpaRepository<Resource, Long> {
    
    List<Resource> findByFacilityIdAndActiveTrue(Long facilityId);

    List<Resource> findByFacilityIdInAndActiveTrueOrderByFacilityIdAscNameAsc(List<Long> facilityIds);
}
