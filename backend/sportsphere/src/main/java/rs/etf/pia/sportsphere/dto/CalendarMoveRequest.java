package rs.etf.pia.sportsphere.dto;

import java.time.LocalDateTime;

public class CalendarMoveRequest {

    private LocalDateTime startTime;

    public CalendarMoveRequest() {
    }

    public CalendarMoveRequest(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
