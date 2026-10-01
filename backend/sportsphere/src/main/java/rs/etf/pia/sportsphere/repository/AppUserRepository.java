package rs.etf.pia.sportsphere.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.AppUser;
import java.util.List;


public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    
    Optional<AppUser> findByUsername(String username);

    Optional<AppUser> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<AppUser> findByStatus(String status);
    List<AppUser> findByRole(String role);
    List<AppUser> findByRoleAndStatus(String role, String status);

    Optional<AppUser> findByUsernameOrEmail(String username, String email);
}
