package rs.etf.pia.sportsphere.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import rs.etf.pia.sportsphere.entity.Facility;

public interface FacilityRepository extends JpaRepository<Facility, Long> {
    
    long countByStatus(String status);

    List<Facility> findTop3ByStatusOrderByLikeCountDesc(String status);

    Optional<Facility> findByIdAndStatus(Long id, String status);

    List<Facility> findByStatus(String status);

    @Query("""
            SELECT DISTINCT f.city
            from Facility f
            where f.status = 'APPROVED'
            ORDER BY f.city 
            """)
    List<String> findApprovedCities();

    List<Facility> findByOrganizationId(Long organizationId);

    List<Facility> findByOrganizationIdIn(List<Long> organizationIds);

    @Query("""
            SELECT DISTINCT f
            FROM Facility f
            WHERE f.status = 'APPROVED'
                AND (:name IS NULL OR LOWER(f.name) LIKE LOWER(CONCAT('%', :name, '%')))
                AND (:city IS NULL OR f.city = :city)
                AND (:sportId IS NULL OR EXISTS(
                    SELECT fs 
                    FROM FacilitySport fs
                    WHERE fs.facilityId = f.id
                        AND fs.sportId = :sportId
                ))
                AND (:resourceType IS NULL OR EXISTS(
                    SELECT r
                    FROM Resource r
                    WHERE r.facilityId = f.id
                        AND r.type = :resourceType
                        AND r.active = true
                ))

            """)
    List<Facility> searchApprovedFacilities(
        @Param("name") String name,
        @Param("city") String city,
        @Param("sportId") Long sportId,
        @Param("resourceType") String resourceType
    );

    
}
