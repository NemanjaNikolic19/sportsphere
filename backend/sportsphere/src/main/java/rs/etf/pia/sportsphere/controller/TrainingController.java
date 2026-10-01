package rs.etf.pia.sportsphere.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.dto.TrainingSessionRequest;
import rs.etf.pia.sportsphere.service.TrainingService;

@RestController
@RequestMapping("/api")
public class TrainingController {
    
    private final TrainingService trainingService;

    public TrainingController(TrainingService trainingService) {
        this.trainingService = trainingService;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping("/trainers")
    public ResponseEntity<?> getTrainers(
        @RequestParam(required = false) Long facilityId,
        @RequestParam(required = false) Long sportId
    ){
        try {
            return ResponseEntity.ok(trainingService.getTrainers(facilityId, sportId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/athlete/{userId}/training-sessions")
    public ResponseEntity<?> createTrainingSession(
        @PathVariable Long userId,
        @RequestBody TrainingSessionRequest request
    ) {
        try {
            return ResponseEntity.ok(trainingService.createTrainingSession(userId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/athlete/{userId}/training-sessions")
    public ResponseEntity<?> getTrainingSessions(@PathVariable Long userId) {
        try {
            return ResponseEntity.ok(trainingService.getTrainingSessions(userId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
