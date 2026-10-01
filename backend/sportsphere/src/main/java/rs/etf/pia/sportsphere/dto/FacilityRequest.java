package rs.etf.pia.sportsphere.dto;

import java.math.BigDecimal;
import java.time.LocalTime;

public class FacilityRequest {
    
    private String name;
    private String city;
    private String address;
    private String description;
    private BigDecimal pricePerHour;
    private LocalTime workingTimeFrom;
    private LocalTime workingTimeTo;
    private Integer allowedNoShows;
    
    public FacilityRequest() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPricePerHour() {
        return pricePerHour;
    }

    public void setPricePerHour(BigDecimal pricePerHour) {
        this.pricePerHour = pricePerHour;
    }

    public LocalTime getWorkingTimeFrom() {
        return workingTimeFrom;
    }

    public void setWorkingTimeFrom(LocalTime workingTimeFrom) {
        this.workingTimeFrom = workingTimeFrom;
    }

    public LocalTime getWorkingTimeTo() {
        return workingTimeTo;
    }

    public void setWorkingTimeTo(LocalTime workingTimeTo) {
        this.workingTimeTo = workingTimeTo;
    }

    public Integer getAllowedNoShows() {
        return allowedNoShows;
    }

    public void setAllowedNoShows(Integer allowedNoShows) {
        this.allowedNoShows = allowedNoShows;
    }

}
