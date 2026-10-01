package rs.etf.pia.sportsphere.entity;

import java.io.Serializable;

public class FacilitySportId implements Serializable {
    
    private Long facilityId;
    private Long sportId;

    public FacilitySportId() {
    }

    public FacilitySportId(Long facilityId, Long sportId) {
        this.facilityId = facilityId;
        this.sportId = sportId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((facilityId == null) ? 0 : facilityId.hashCode());
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
        FacilitySportId other = (FacilitySportId) obj;
        if (facilityId == null) {
            if (other.facilityId != null)
                return false;
        } else if (!facilityId.equals(other.facilityId))
            return false;
        if (sportId == null) {
            if (other.sportId != null)
                return false;
        } else if (!sportId.equals(other.sportId))
            return false;
        return true;
    }

    

}
