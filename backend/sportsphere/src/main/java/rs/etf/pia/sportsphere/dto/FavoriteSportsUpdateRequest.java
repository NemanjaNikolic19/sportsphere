package rs.etf.pia.sportsphere.dto;

import java.util.List;

public class FavoriteSportsUpdateRequest {
    
    private List<Long> sportIds;

    public FavoriteSportsUpdateRequest() {
    }

    public FavoriteSportsUpdateRequest(List<Long> sportIds) {
        this.sportIds = sportIds;
    }

    public List<Long> getSportIds() {
        return sportIds;
    }

    public void setSportIds(List<Long> sportIds) {
        this.sportIds = sportIds;
    }
}
