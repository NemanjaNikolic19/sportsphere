package rs.etf.pia.sportsphere.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.entity.Trainer;
import rs.etf.pia.sportsphere.service.AdminService;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @GetMapping("/users")
    public List<AppUser> getAllUsers(){
        return adminService.getAllUsers();
    }

    @GetMapping("/users/pending")
    public List<AppUser> getPendingUsers() {
        return adminService.getPendingUsers();
    }
    
    @PutMapping("/users/{id}/approve")
    public ResponseEntity<?> approveUser(@PathVariable Long id) {
        try {
            AppUser user = adminService.approveUser(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}/reject")
    public ResponseEntity<?> rejectUser(@PathVariable Long id){
        try {
            AppUser user = adminService.rejectUser(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/users/{id}")
    public ResponseEntity<?> updateUser(@PathVariable Long id, @RequestBody AppUser updatedUser) {
        try{
            AppUser user = adminService.updateUser(id, updatedUser);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        } 
    }

    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            AppUser user = adminService.deleteUser(id);
            return ResponseEntity.ok(user);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/facilities/pending")
    public List<Facility> getPendingFacilities(){
        return adminService.getPendingFacilities();
    }

    @PutMapping("/facilities/{id}/approve")
    public ResponseEntity<?> approveFacility(@PathVariable Long id){
        try{
            Facility facility = adminService.approveFacility(id);
            return ResponseEntity.ok(facility);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/facilities/{id}/reject")
    public ResponseEntity<?> rejectFacility(@PathVariable Long id) {
        try{
            Facility facility = adminService.rejectFacility(id);
            return ResponseEntity.ok(facility);
        } catch(IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/sports")
    public List<Sport> getSports() {
        return adminService.getSports();
    }

    @PostMapping("/sports")
    public ResponseEntity<?> createSport(@RequestBody Map<String, String> data) {
        try {
            return ResponseEntity.ok(
                adminService.createSport(data.get("name"))
            );
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/trainers")
    public List<Trainer> getTrainers() {
        return adminService.getTrainers();
    }

    @PutMapping("/trainers/{trainerId}/deactivate")
    public ResponseEntity<?> deactivateTrainer(@PathVariable Long trainerId) {
        try {
            return ResponseEntity.ok(
                adminService.deactivateTrainer(trainerId)
            );
        } catch(IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                Map.of("message", e.getMessage())
            );
        }
    }

    @GetMapping("/ping")
    public Map<String, String> ping(){
        return Map.of("message", "Admin controller works");
    }
}
