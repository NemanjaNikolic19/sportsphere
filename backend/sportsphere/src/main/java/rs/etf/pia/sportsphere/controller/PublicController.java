package rs.etf.pia.sportsphere.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.FacilityImage;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.service.FacilityReviewService;
import rs.etf.pia.sportsphere.service.PublicService;

@RestController
@RequestMapping("/api/public")
public class PublicController {

    private final PublicService publicService;
    private final FacilityReviewService facilityReviewService;

    private ResponseEntity<Map<String, String>> error(
        HttpStatus status,
        String message
    ) {
        return ResponseEntity
            .status(status)
            .body(Map.of("message", message));
    }

    public PublicController(PublicService publicService, FacilityReviewService facilityReviewService) {
        this.publicService = publicService;
        this.facilityReviewService = facilityReviewService;
    }

    @GetMapping("/facilities/active-count")
    public long getActiveFacilityCount() {
        return publicService.getActiveFacilityCount();
    }

    @GetMapping("/facilities/top3")
    public List<Facility> getTop3Facilities() {
        return publicService.getTop3Facilities();
    }

    @GetMapping("/cities")
    public List<String> getApprovedCities() {
        return publicService.getApprovedCities();
    }

    @GetMapping("/sports")
    public List<Sport> getAllSports(){
        return publicService.getAllSports();
    } 

    @GetMapping("/facilities/search")
    public List<Facility> searchFacilities(
        @RequestParam(required = false) String name,
        @RequestParam(required = false) String city,
        @RequestParam(required = false) Long sportId,
        @RequestParam(required = false) String resourceType
    ) {
        return publicService.searchFacilities(name, city, sportId, resourceType);
    }

    @GetMapping("/facilities/{id}")
    public ResponseEntity<Facility> getFacilityById(@PathVariable Long id) {
        return publicService.getFacilityById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/facilities/{id}/resources")
    public ResponseEntity<List<Resource>> getFacilityResources(@PathVariable Long id) {
        if(publicService.getFacilityResources(id).isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(publicService.getFacilityResources(id));
    }

    @GetMapping("/facilities/{id}/images")
    public ResponseEntity<List<FacilityImage>> getFacilityImages(@PathVariable Long id) {
        if(publicService.getFacilityImages(id).isEmpty()){
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(publicService.getFacilityImages(id));
    }

    @GetMapping("/facilities/{facilityId}/reviews")
    public ResponseEntity<?> getLatestFacilityReviews(@PathVariable Long facilityId) {
        try {
            return ResponseEntity.ok(
                facilityReviewService.getLatestReviews(
                    facilityId
                )
            );
        } catch(IllegalArgumentException e) {
            return error(
                HttpStatus.BAD_REQUEST,
                e.getMessage()
            );
        }
    }

}
