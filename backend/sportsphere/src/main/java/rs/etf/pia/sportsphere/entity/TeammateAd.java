package rs.etf.pia.sportsphere.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "teammate_ad")
public class TeammateAd {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "athlete_id")
    private Long athleteId;

    @Column(name = "sport_id")
    private Long sportId;

    @Column(name = "city")
    private String city;

    @Column(name = "play_date")
    private LocalDate playDate;

    @Column(name = "start_time")
    private LocalTime startTime;

    @Column(name = "end_time")
    private LocalTime endTime;

    @Column(name = "needed_players")
    private Integer neededPlayers;

    @Column(name = "accepted_players")
    private Integer acceptedPlayers;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public TeammateAd() {
    }

    public TeammateAd(Long id, Long athleteId, Long sportId, String city, LocalDate playDate, LocalTime startTime,
            LocalTime endTime, Integer neededPlayers, Integer acceptedPlayers, String status, LocalDateTime createdAt) {
        this.id = id;
        this.athleteId = athleteId;
        this.sportId = sportId;
        this.city = city;
        this.playDate = playDate;
        this.startTime = startTime;
        this.endTime = endTime;
        this.neededPlayers = neededPlayers;
        this.acceptedPlayers = acceptedPlayers;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public LocalDate getPlayDate() {
        return playDate;
    }

    public void setPlayDate(LocalDate playDate) {
        this.playDate = playDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    public Integer getNeededPlayers() {
        return neededPlayers;
    }

    public void setNeededPlayers(Integer neededPlayers) {
        this.neededPlayers = neededPlayers;
    }

    public Integer getAcceptedPlayers() {
        return acceptedPlayers;
    }

    public void setAcceptedPlayers(Integer acceptedPlayers) {
        this.acceptedPlayers = acceptedPlayers;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    

}
