package rs.etf.pia.sportsphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.Organization;


public interface OrganizationRepository extends JpaRepository<Organization, Long> {
    
    Optional<Organization> findByRegistrationNumber(String registrationNumber);

    Optional<Organization> findByTaxNumber(String taxNumber);
}
