package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.entity.OrganizationEmployeeId;

public interface OrganizationEmployeeRepository extends JpaRepository<OrganizationEmployee, OrganizationEmployeeId> {
    
    List<OrganizationEmployee> findByEmployeeId(Long employeeId);

    boolean existsByEmployeeIdAndOrganizationId(Long employeeId, Long organizationId);

    long countByOrganizationId(Long organizationId);
}
