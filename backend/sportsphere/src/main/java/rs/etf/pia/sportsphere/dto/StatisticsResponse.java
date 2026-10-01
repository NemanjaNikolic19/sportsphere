package rs.etf.pia.sportsphere.dto;

import java.math.BigDecimal;
import java.util.Map;

public class StatisticsResponse {

    private Map<String, Long> playedBySport;
    private Map<String, Long> reservedBySport;
    private Map<String, Long> monthlyActivity;

    private Map<String, BigDecimal>
        equipmentSpendingByAthlete;

    private BigDecimal
        totalEquipmentSpendingAllAthletes;

    public StatisticsResponse() {
    }

    public StatisticsResponse(
            Map<String, Long> playedBySport,
            Map<String, Long> reservedBySport,
            Map<String, Long> monthlyActivity,
            Map<String, BigDecimal>
                equipmentSpendingByAthlete,
            BigDecimal
                totalEquipmentSpendingAllAthletes) {

        this.playedBySport = playedBySport;
        this.reservedBySport = reservedBySport;
        this.monthlyActivity = monthlyActivity;
        this.equipmentSpendingByAthlete =
            equipmentSpendingByAthlete;
        this.totalEquipmentSpendingAllAthletes =
            totalEquipmentSpendingAllAthletes;
    }

    public Map<String, Long> getPlayedBySport() {
        return playedBySport;
    }

    public Map<String, Long> getReservedBySport() {
        return reservedBySport;
    }

    public Map<String, Long> getMonthlyActivity() {
        return monthlyActivity;
    }

    public Map<String, BigDecimal>
            getEquipmentSpendingByAthlete() {

        return equipmentSpendingByAthlete;
    }

    public BigDecimal
            getTotalEquipmentSpendingAllAthletes() {

        return totalEquipmentSpendingAllAthletes;
    }
}