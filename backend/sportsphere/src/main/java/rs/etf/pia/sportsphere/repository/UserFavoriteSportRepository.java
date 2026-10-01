package rs.etf.pia.sportsphere.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import rs.etf.pia.sportsphere.entity.UserFavoriteSport;
import rs.etf.pia.sportsphere.entity.UserFavoriteSportId;

public interface UserFavoriteSportRepository extends JpaRepository<UserFavoriteSport, UserFavoriteSportId> {
    
    List<UserFavoriteSport> findByUserId(Long userId);

    void deleteByUserId(Long userId);

    void deleteByUserIdAndSportId(Long userId, Long sportId);
}
