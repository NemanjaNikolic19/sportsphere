package rs.etf.pia.sportsphere.controller;

import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.service.ReportService;

@RestController
@RequestMapping("/api")
public class ReportController {
    
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping(
        value = "/employee/{employeeId}/reports/monthly",
        produces = MediaType.APPLICATION_PDF_VALUE
    )
    public ResponseEntity<?> generateMonthlyReport(
        @PathVariable Long employeeId,
        @RequestParam int year,
        @RequestParam int month
    ) {
        try {
            byte[] pdf = reportService.generateMonthlyReport(employeeId, year, month);

            String filename = String.format(
                "sportsphere-report-%04d-%02d.pdf", 
                year,
                month
            );

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + filename + "\""
                    )
                    .body(pdf);
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch(IllegalStateException e) {
            return error(HttpStatus.INTERNAL_SERVER_ERROR, e.getMessage());
        }
    }
}
