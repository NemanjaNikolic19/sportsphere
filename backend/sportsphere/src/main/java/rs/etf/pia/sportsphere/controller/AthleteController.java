package rs.etf.pia.sportsphere.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import rs.etf.pia.sportsphere.dto.FacilityReviewRequest;
import rs.etf.pia.sportsphere.dto.FavoriteSportsUpdateRequest;
import rs.etf.pia.sportsphere.dto.ProfileUpdateRequest;
import rs.etf.pia.sportsphere.dto.ReservationRequest;
import rs.etf.pia.sportsphere.service.AthleteService;
import rs.etf.pia.sportsphere.service.FacilityReviewService;
import rs.etf.pia.sportsphere.service.StatisticsService;

@RestController
@RequestMapping("/api")
public class AthleteController {
    
    private final AthleteService athleteService;
    private final FacilityReviewService facilityReviewService;
    private final StatisticsService statisticsService;

    public AthleteController(AthleteService athleteService,FacilityReviewService facilityReviewService, StatisticsService statisticsService) {
        this.athleteService = athleteService;
        this.facilityReviewService = facilityReviewService;
        this.statisticsService = statisticsService;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping("/athlete/{userId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(athleteService.getProfile(userId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/profile")
    public ResponseEntity<?> updateProfile(@PathVariable Long userId, @RequestBody ProfileUpdateRequest request){
        try {
            return ResponseEntity.ok(athleteService.updateProfile(userId, request));
        } catch(IllegalArgumentException e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/profile-image")
    public ResponseEntity<?> updateProfileImage(
        @PathVariable Long userId,
        @RequestParam("profileImage")
            MultipartFile profileImage
    ) {
        try {
            return ResponseEntity.ok(
                athleteService.updateProfileImage(
                    userId,
                    profileImage
                )
            );
        } catch(IllegalArgumentException e) {
            return error(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @DeleteMapping("/athlete/{userId}/profile-image")
    public ResponseEntity<?> removeProfileImage(
        @PathVariable Long userId
    ) {
        try {
            return ResponseEntity.ok(
                athleteService.removeProfileImage(userId)
            );
        } catch(IllegalArgumentException e) {
            return error(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @GetMapping("/athlete/{userId}/favorite-sports")
    public ResponseEntity<?> getFavoriteSports(@PathVariable Long userId){
        try {
            return ResponseEntity.ok(athleteService.getFavoriteSports(userId));
        } catch(IllegalArgumentException e){
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/favorite-sports")
    public ResponseEntity<?> updateFavoriteSports(@PathVariable Long userId, @RequestBody FavoriteSportsUpdateRequest request){
        try {
            return ResponseEntity.ok(athleteService.updateFavoriteSports(userId, request));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/athlete/{userId}/reservations")
    public ResponseEntity<?> getReservations(@PathVariable Long userId){
        try {
            return ResponseEntity.ok(athleteService.getReservations(userId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/athlete/{userId}/reservations")
    public ResponseEntity<?> createReservation(@PathVariable Long userId, @RequestBody ReservationRequest request){
        try {
            return ResponseEntity.ok(athleteService.createReservation(userId, request));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/athlete/{userId}/reservations/{reservationId}/cancel")
    public ResponseEntity<?> cancelReservation(@PathVariable Long userId, @PathVariable Long reservationId) {
        try {
            return ResponseEntity.ok(athleteService.cancelReservation(userId, reservationId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/resources/{resourceId}/reservations")
    public ResponseEntity<?> getResourceReservations(@PathVariable Long resourceId){
        try {
            return ResponseEntity.ok(athleteService.getResourceReservations(resourceId));
        } catch (IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/athlete/{athleteId}/facilities/{facilityId}/reviews/remaining")
    public ResponseEntity<?> getRemainingFacilityReviews(
        @PathVariable Long athleteId,
        @PathVariable Long facilityId
    ) {
        try {
            long remainingReviews = facilityReviewService.getRemainingReviewCount(athleteId, facilityId);

            return ResponseEntity.ok(Map.of("remainingReviews", remainingReviews));

        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/athlete/{athleteId}/facilities/{facilityId}/reviews")
    public ResponseEntity<?> addFacilityReview(
        @PathVariable Long athleteId,
        @PathVariable Long facilityId,
        @RequestBody FacilityReviewRequest request
    ) {
        try {
            return ResponseEntity.ok(facilityReviewService.addReview(athleteId, facilityId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }

    @GetMapping("/athlete/{athleteId}/statistics")
    public ResponseEntity<?> getStatistics(@PathVariable Long athleteId) {
        try {
            return ResponseEntity.ok(statisticsService.getStatistics(athleteId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST,e.getMessage());
        }
    }
}
