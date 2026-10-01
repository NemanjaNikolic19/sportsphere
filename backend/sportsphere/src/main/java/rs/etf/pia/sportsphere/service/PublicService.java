package rs.etf.pia.sportsphere.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.FacilityImage;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.repository.FacilityImageRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.ResourceRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;

@Service
public class PublicService {
    
    private static final String APPROVED_STATUS = "APPROVED";

    private final FacilityRepository facilityRepository;
    private final FacilityImageRepository facilityImageRepository;
    private final ResourceRepository resourceRepository;
    private final SportRepository sportRepository;

    public PublicService(FacilityRepository facilityRepository, FacilityImageRepository facilityImageRepository,
            ResourceRepository resourceRepository, SportRepository sportRepository) {
        this.facilityRepository = facilityRepository;
        this.facilityImageRepository = facilityImageRepository;
        this.resourceRepository = resourceRepository;
        this.sportRepository = sportRepository;
    }

    public long getActiveFacilityCount(){
        return facilityRepository.countByStatus(APPROVED_STATUS);
    }

    public List<Facility> getTop3Facilities(){
        return facilityRepository.findTop3ByStatusOrderByLikeCountDesc(APPROVED_STATUS);
    }

    public List<String> getApprovedCities(){
        return facilityRepository.findApprovedCities();
    }

    public List<Sport> getAllSports(){
        return sportRepository.findAll();
    }

    public List<Facility> searchFacilities(String name, String city, Long sportId, String resourceType) {
        String normalizedName = normalizeString(name);
        String normalizedCity = normalizeString(city);
        String normalizedResourceType = normalizeString(resourceType);

        return facilityRepository.searchApprovedFacilities(normalizedName, normalizedCity, sportId, normalizedResourceType);
    }

    public Optional<Facility> getFacilityById(Long id){
        return facilityRepository.findByIdAndStatus(id, APPROVED_STATUS);
    }

    public List<Resource> getFacilityResources(Long facilityId){
        return resourceRepository.findByFacilityIdAndActiveTrue(facilityId);
    }

    public List<FacilityImage> getFacilityImages(Long facilityId) {
        return facilityImageRepository.findByFacilityId(facilityId);
    }

    public String normalizeString(String value){
        if(value == null || value.trim().isEmpty())
            return null;

        return value.trim();
    }
}
