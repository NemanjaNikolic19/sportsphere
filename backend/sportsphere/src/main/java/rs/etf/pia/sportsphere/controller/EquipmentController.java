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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.dto.EquipmentOrderRequest;
import rs.etf.pia.sportsphere.dto.EquipmentRequest;
import rs.etf.pia.sportsphere.service.EquipmentService;

@RestController
@RequestMapping("/api")
public class EquipmentController {
    
    private final EquipmentService equipmentService;

    public EquipmentController(EquipmentService equipmentService) {
        this.equipmentService = equipmentService;
    }

    private ResponseEntity<Map<String, String>> error(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(Map.of("message", message));
    }

    @GetMapping("/equipment")
    public ResponseEntity<?> getEquipment(
        @RequestParam(required = false) Long sportId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.getEquipment(sportId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @PostMapping("/athlete/{userId}/orders")
    public ResponseEntity<?> createOrder(
        @PathVariable Long userId,
        @RequestBody EquipmentOrderRequest request
    ) {
        try {
            return ResponseEntity.ok(equipmentService.createEquipmentOrder(userId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @GetMapping("/athlete/{userId}/orders")
    public ResponseEntity<?> getAthleteOrders(
        @PathVariable Long userId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.getAthleteOrders(userId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }
    
    @PutMapping("/athlete/{userId}/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelAthleteOrder(
        @PathVariable Long userId,
        @PathVariable Long orderId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.cancelAthleteOrder(userId, orderId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping("/employee/{employeeId}/equipment")
    public ResponseEntity<?> getEmployeeEquipment(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.getEmployeeEquipment(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PostMapping("/employee/{employeeId}/equipment")
    public ResponseEntity<?> createEquipment(
        @PathVariable Long employeeId,
        @RequestBody EquipmentRequest request 
    ) {
        try {
            return ResponseEntity.ok(equipmentService.createEquipment(employeeId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/employee/{employeeId}/equipment/{equipmentId}")
    public ResponseEntity<?> updateEquipment(
        @PathVariable Long employeeId,
        @PathVariable Long equipmentId,
        @RequestBody EquipmentRequest request 
    ) {
        try {
            return ResponseEntity.ok(equipmentService.updateEquipment(employeeId, equipmentId, request));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }


    @GetMapping("/employee/{employeeId}/orders")
    public ResponseEntity<?> getEmployeeOrders(
        @PathVariable Long employeeId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.getEmployeeOrders(employeeId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/employee/{employeeId}/orders/{orderId}/accept")
    public ResponseEntity<?> acceptOrder(
        @PathVariable Long employeeId,
        @PathVariable Long orderId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.acceptOrder(employeeId, orderId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/employee/{employeeId}/orders/{orderId}/picked-up")
    public ResponseEntity<?> markOrderPickedUp(
        @PathVariable Long employeeId,
        @PathVariable Long orderId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.markOrderPickedUp(employeeId, orderId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @PutMapping("/employee/{employeeId}/orders/{orderId}/cancel")
    public ResponseEntity<?> cancelEmployeeOrder(
        @PathVariable Long employeeId,
        @PathVariable Long orderId
    ) {
        try {
            return ResponseEntity.ok(equipmentService.cancelEmployeeOrder(employeeId, orderId));
        } catch(IllegalArgumentException e) {
            return error(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

}
