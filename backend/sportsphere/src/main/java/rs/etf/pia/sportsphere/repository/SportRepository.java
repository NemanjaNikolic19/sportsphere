package rs.etf.pia.sportsphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.Sport;

public interface SportRepository extends JpaRepository<Sport, Long> {
    
    Optional<Sport> findByName(String name);

    boolean existsByNameIgnoreCase(String name);
}
