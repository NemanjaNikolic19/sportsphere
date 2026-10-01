package rs.etf.pia.sportsphere.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@Table(name = "facility_sport")
@IdClass(FacilitySportId.class)
public class FacilitySport {
    
    @Id
    @Column(name = "facility_id")
    private Long facilityId;
    @Id
    @Column(name = "sport_id")
    private Long sportId;

    public FacilitySport() {
    }

    public FacilitySport(Long facilityId, Long sportId) {
        this.facilityId = facilityId;
        this.sportId = sportId;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    
    
}
