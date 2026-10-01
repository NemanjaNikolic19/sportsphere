package rs.etf.pia.sportsphere.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "facility")
public class Facility {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    
    @Column(name = "organization_id")
    private Long organizationId;
    
    @Column(name = "name")
    private String name;
    
    @Column(name = "city")
    private String city;
    
    @Column(name = "address")
    private String address;
    
    @Column(name = "description")
    private String description;
    
    @Column(name = "price_per_hour")
    private BigDecimal pricePerHour;
    
    @Column(name = "working_time_from")
    private LocalTime workingTimeFrom;
    
    @Column(name = "working_time_to")
    private LocalTime workingTimeTo;
    
    @Column(name = "allowed_no_shows")
    private Integer allowedNoShows;
    
    @Column(name = "status")
    private String status;
    
    @Column(name = "like_count")
    private Integer likeCount;
    
    @Column(name = "dislike_count")
    private Integer dislikeCount;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public Facility() {
    }

    public Facility(Long id, Long organizationId, String name, String city, String address, String description,
            BigDecimal pricePerHour, LocalTime workingTimeFrom, LocalTime workingTimeTo, Integer allowedNoShows,
            String status, Integer likeCount, Integer dislikeCount, LocalDateTime createdAt) {
        this.id = id;
        this.organizationId = organizationId;
        this.name = name;
        this.city = city;
        this.address = address;
        this.description = description;
        this.pricePerHour = pricePerHour;
        this.workingTimeFrom = workingTimeFrom;
        this.workingTimeTo = workingTimeTo;
        this.allowedNoShows = allowedNoShows;
        this.status = status;
        this.likeCount = likeCount;
        this.dislikeCount = dislikeCount;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrganizationId() {
        return organizationId;
    }

    public void setOrganizationId(Long organizationId) {
        this.organizationId = organizationId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Integer getLikeCount() {
        return likeCount;
    }

    public void setLikeCount(Integer likeCount) {
        this.likeCount = likeCount;
    }

    public Integer getDislikeCount() {
        return dislikeCount;
    }

    public void setDislikeCount(Integer dislikeCount) {
        this.dislikeCount = dislikeCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    

}
