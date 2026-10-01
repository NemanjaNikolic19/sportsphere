package rs.etf.pia.sportsphere.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.dto.PromotionRequest;
import rs.etf.pia.sportsphere.service.PromotionService;

@RestController
@RequestMapping("/api")
public class PromotionController {
    
    private final PromotionService promotionService;

    public PromotionController(PromotionService promotionService) {
        this.promotionService = promotionService;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping("/promotions/active")
    public ResponseEntity<?> getActivePromotions() {
        try {
            return ResponseEntity.ok(promotionService.getActivePromotions());
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/promotions/upcoming")
    public ResponseEntity<?> getUpcomingPromotions() {
        try {
            return ResponseEntity.ok(promotionService.getUpcomingPromotions());
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/employee/{employeeId}/promotions")
    public ResponseEntity<?> getEmployeePromotions(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(promotionService.getEmployeePromotions(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/employee/{employeeId}/promotions")
    public ResponseEntity<?> createPromotion(
        @PathVariable Long employeeId,
        @RequestBody PromotionRequest request
    ) {
        try {
            return ResponseEntity.ok(promotionService.createPromotion(employeeId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PutMapping("/employee/{employeeId}/promotions/{promotionId}")
    public ResponseEntity<?> updatePromotion(
        @PathVariable Long employeeId,
        @PathVariable Long promotionId,
        @RequestBody PromotionRequest request
    ) {
        try {
            return ResponseEntity.ok(promotionService.updatePromotion(employeeId, promotionId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
