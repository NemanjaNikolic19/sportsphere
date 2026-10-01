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

import rs.etf.pia.sportsphere.dto.TeammateAdRequest;
import rs.etf.pia.sportsphere.service.TeammateAdService;

@RestController
@RequestMapping("/api")
public class TeammateAdController {
    
    private final TeammateAdService teammateAdService;

    public TeammateAdController(TeammateAdService teammateAdService) {
        this.teammateAdService = teammateAdService;
    }

    public ResponseEntity<Map<String, String>> error(HttpStatus status, String message){
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping("/teammate-ads/active")
    public ResponseEntity<?> getActiveAds() {
        try {
            return ResponseEntity.ok(teammateAdService.getActiveAds());
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/athlete/{userId}/teammate-ads")
    public ResponseEntity<?> createAd(
        @PathVariable Long userId, 
        @RequestBody TeammateAdRequest request
    ) {
        try {
            return ResponseEntity.ok(teammateAdService.createAd(userId, request));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/athlete/{userId}/teammate-ads/{adId}/join")
    public ResponseEntity<?> joinAd(
        @PathVariable Long userId, 
        @PathVariable Long adId
    ) {
        try {
            return ResponseEntity.ok(teammateAdService.joinAd(userId, adId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/athlete/{userId}/teammate-ads/requests")
    public ResponseEntity<?> getRequestsForOwnerAds(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(teammateAdService.getRequestsForOwnerAds(userId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/teammate-requests/{requestId}/approve")
    public ResponseEntity<?> approveRequest(
        @PathVariable Long userId,
        @PathVariable Long requestId
    ) {
        try {
            return ResponseEntity.ok(teammateAdService.approveRequest(userId, requestId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/teammate-requests/{requestId}/reject")
    public ResponseEntity<?> rejectRequest(
        @PathVariable Long userId,
        @PathVariable Long requestId
    ) {
        try {
            return ResponseEntity.ok(teammateAdService.rejectRequest(userId, requestId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/teammate-ads/{adId}/close")
    public ResponseEntity<?> closeAd(
        @PathVariable Long userId,
        @PathVariable Long adId
    ) {
        try {
            return ResponseEntity.ok(teammateAdService.closeAd(userId, adId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
