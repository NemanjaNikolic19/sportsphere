package rs.etf.pia.sportsphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "user_favorite_sport")
@IdClass(UserFavoriteSportId.class)
public class UserFavoriteSport {
    
    @Id
    @Column(name = "user_id")
    private Long userId;
    @Id
    @Column(name = "sport_id")
    private Long sportId;

    public UserFavoriteSport() {
    }

    public UserFavoriteSport(Long userId, Long sportId) {
        this.userId = userId;
        this.sportId = sportId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

}
