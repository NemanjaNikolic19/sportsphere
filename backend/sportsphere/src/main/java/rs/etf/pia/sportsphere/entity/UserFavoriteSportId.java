package rs.etf.pia.sportsphere.entity;

import java.io.Serializable;

public class UserFavoriteSportId implements Serializable {
    
    private Long userId;
    private Long sportId;

    public UserFavoriteSportId() {
    }

    public UserFavoriteSportId(Long userId, Long sportId) {
        this.userId = userId;
        this.sportId = sportId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((userId == null) ? 0 : userId.hashCode());
        result = prime * result + ((sportId == null) ? 0 : sportId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        UserFavoriteSportId other = (UserFavoriteSportId) obj;
        if (userId == null) {
            if (other.userId != null)
                return false;
        } else if (!userId.equals(other.userId))
            return false;
        if (sportId == null) {
            if (other.sportId != null)
                return false;
        } else if (!sportId.equals(other.sportId))
            return false;
        return true;
    }

    
}
