package rs.etf.pia.sportsphere.controller;

import java.io.IOException;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import rs.etf.pia.sportsphere.dto.CalendarMoveRequest;
import rs.etf.pia.sportsphere.dto.FacilityRequest;
import rs.etf.pia.sportsphere.dto.ProfileUpdateRequest;
import rs.etf.pia.sportsphere.service.EmployeeService;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RestController
@RequestMapping("/api/employee")
public class EmployeeController {
    
    private final EmployeeService employeeService;
    private final ObjectMapper objectMapper;

    public EmployeeController(EmployeeService employeeService, ObjectMapper objectMapper) {
        this.employeeService = employeeService;
        this.objectMapper = objectMapper;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message",message));
    }

    @GetMapping("/{employeeId}/profile")
    public ResponseEntity<?> getProfile(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(employeeService.getProfile(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/profile")
    public ResponseEntity<?> updateProfile(
        @PathVariable Long employeeId,
        @RequestBody ProfileUpdateRequest request
    ) {
        try {
            return ResponseEntity.ok(employeeService.updateProfile(employeeId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{employeeId}/facilities")
    public ResponseEntity<?> getFacilities(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(employeeService.getFacilities(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{employeeId}/facilities")
    public ResponseEntity<?> createFacility(
        @PathVariable Long employeeId,
        @RequestBody FacilityRequest request
    ) {
        try {
            return ResponseEntity.ok(employeeService.createFacility(employeeId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/facilities/{facilityId}")
    public ResponseEntity<?> updateFacility(
        @PathVariable Long employeeId,
        @PathVariable Long facilityId,
        @RequestBody FacilityRequest request
    ) {
        try {
            return ResponseEntity.ok(employeeService.updateFacility(employeeId, facilityId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/{employeeId}/facilities/import-json")
    public ResponseEntity<?> importFacilitiesFromJson(
        @PathVariable Long employeeId,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            if(file == null || file.isEmpty()) {
                return error(HttpStatus.BAD_REQUEST, "JSON file is required");
            }

            JsonNode root = objectMapper.readTree(file.getInputStream());

            List<FacilityRequest> requests;

            if(root.isArray()) {
                requests = objectMapper.convertValue(
                    root,
                    new TypeReference<List<FacilityRequest>> () {}
                );
            } else {
                FacilityRequest request = objectMapper.treeToValue(root, FacilityRequest.class);
                requests = List.of(request);
            }

            return ResponseEntity.ok(employeeService.importFacilitiesFromJson(employeeId, requests));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        } catch(IOException e) {
            return error(HttpStatus.BAD_REQUEST, "Invalid JSON file");
        }
    }
    
    @GetMapping("/{employeeId}/reservations")
    public ResponseEntity<?> getReservations(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(employeeService.getReservations(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/reservations/{reservationId}/confirm")
    public ResponseEntity<?> confirmReservation(
        @PathVariable Long employeeId,
        @PathVariable Long reservationId
    ) {
        try {
            return ResponseEntity.ok(employeeService.confirmReservation(employeeId, reservationId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/reservations/{reservationId}/no-show")
    public ResponseEntity<?> noShowReservation(
        @PathVariable Long employeeId,
        @PathVariable Long reservationId
    ) {
        try {
            return ResponseEntity.ok(employeeService.noShowReservation(employeeId, reservationId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/profile-image")
    public ResponseEntity<?> updateProfileImage(
        @PathVariable Long employeeId,
        @RequestParam("profileImage")
            MultipartFile profileImage
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.updateProfileImage(
                    employeeId,
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

    @DeleteMapping("/{employeeId}/profile-image")
    public ResponseEntity<?> removeProfileImage(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.removeProfileImage(
                    employeeId
                )
            );
        } catch(IllegalArgumentException e) {
            return error(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

    @PutMapping("/{employeeId}/reservations/{reservationId}/move")
    public ResponseEntity<?> moveReservation(
        @PathVariable Long employeeId,
        @PathVariable Long reservationId,
        @RequestBody CalendarMoveRequest request
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.moveReservation(employeeId, reservationId, request)
            );
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{employeeId}/training-sessions")
    public ResponseEntity<?> getTrainingSessions(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(employeeService.getTrainingSessions(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/training-sessions/{sessionId}/confirm")
    public ResponseEntity<?> confirmTrainingSession(
        @PathVariable Long employeeId,
        @PathVariable Long sessionId
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.confirmTrainingSession(employeeId, sessionId)
            );
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/training-sessions/{sessionId}/no-show")
    public ResponseEntity<?> noShowTrainingSession(
        @PathVariable Long employeeId,
        @PathVariable Long sessionId
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.noShowTrainingSession(employeeId, sessionId)
            );
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/{employeeId}/training-sessions/{sessionId}/move")
    public ResponseEntity<?> moveTrainingSession(
        @PathVariable Long employeeId,
        @PathVariable Long sessionId,
        @RequestBody CalendarMoveRequest request
    ) {
        try {
            return ResponseEntity.ok(
                employeeService.moveTrainingSession(employeeId, sessionId, request)
            );
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/{employeeId}/calendar")
    public ResponseEntity<?> getCalendar(@PathVariable Long employeeId) {
        try {
            return ResponseEntity.ok(employeeService.getCalendar(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
}
