package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    
    List<Equipment> findByActiveTrueOrderByNameAsc();

    List<Equipment> findBySportIdAndActiveTrueOrderByNameAsc(Long sportId);

    List<Equipment> findByFacilityIdAndActiveTrueOrderByNameAsc(Long facilityId);

    List<Equipment> findByFacilityIdInOrderByNameAsc(List<Long> facilityIds);
}
