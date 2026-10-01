package rs.etf.pia.sportsphere.service;

import java.util.List;

import org.springframework.stereotype.Service;

import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.entity.Trainer;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;
import rs.etf.pia.sportsphere.repository.TrainerRepository;

@Service
public class AdminService {
    
    private static final String STATUS_PENDING = "PENDING";
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_REJECTED = "REJECTED";
    private static final String STATUS_DELETED = "DELETED";

    private static final String FACILITY_STATUS_PENDING = "PENDING";
    private static final String FACILITY_STATUS_APPROVED = "APPROVED";
    private static final String FACILITY_STATUS_REJECTED = "REJECTED";

    private final AppUserRepository appUserRepository;
    private final FacilityRepository facilityRepository;
    private final SportRepository sportRepository;
    private final TrainerRepository trainerRepository;

    
    public AdminService(AppUserRepository appUserRepository, FacilityRepository facilityRepository,
            SportRepository sportRepository, TrainerRepository trainerRepository) {
        this.appUserRepository = appUserRepository;
        this.facilityRepository = facilityRepository;
        this.sportRepository = sportRepository;
        this.trainerRepository = trainerRepository;
    }

    public List<AppUser> getAllUsers(){
        return appUserRepository.findAll();
    }

    public List<AppUser> getPendingUsers() {
        return appUserRepository.findByStatus(STATUS_PENDING);
    }

    public AppUser approveUser(Long userId) {
        AppUser user = getUserOrThrow(userId);

        user.setStatus(STATUS_ACTIVE);

        return appUserRepository.save(user);
    }

    public AppUser rejectUser(Long userId) {
        AppUser user = getUserOrThrow(userId);

        user.setStatus(STATUS_REJECTED);

        return appUserRepository.save(user);
    }

    public AppUser updateUser(Long userId, AppUser updatedUser) {
        AppUser user = getUserOrThrow(userId);

        user.setFirstName(updatedUser.getFirstName());
        user.setLastName(updatedUser.getLastName());
        user.setPhone(updatedUser.getPhone());
        user.setEmail(updatedUser.getEmail());
        user.setProfileImagePath(updatedUser.getProfileImagePath());

        if(updatedUser.getRole() != null)
            user.setRole(updatedUser.getRole());

        if(updatedUser.getStatus() != null)
            user.setStatus(updatedUser.getStatus());

        return appUserRepository.save(user);
    }

    public AppUser deleteUser(Long userId) {
        AppUser user = getUserOrThrow(userId);

        user.setStatus(STATUS_DELETED);

        return appUserRepository.save(user);
    }

    public List<Facility> getPendingFacilities() {
        return facilityRepository.findByStatus(FACILITY_STATUS_PENDING);
    }

    public Facility approveFacility(Long facilityId) {
        Facility facility = getFacilityOrThrow(facilityId);

        facility.setStatus(FACILITY_STATUS_APPROVED);

        return facilityRepository.save(facility);
    }

    public Facility rejectFacility(Long facilityId) {
        Facility facility = getFacilityOrThrow(facilityId);

        facility.setStatus(FACILITY_STATUS_REJECTED);

        return facilityRepository.save(facility);
    }

    private AppUser getUserOrThrow(Long userID) {
        return appUserRepository.findById(userID)
                .orElseThrow(() -> new  IllegalArgumentException("User not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    public List<Sport> getSports() {
        return sportRepository.findAll();
    }

    public Sport createSport(String name) {
        if(name == null || name.isBlank()) {
            throw new IllegalArgumentException("Sport name is required");
        }

        String trimmedName = name.trim();

        if(sportRepository.existsByNameIgnoreCase(trimmedName)) {
            throw new IllegalArgumentException("Sport already exists");
        }

        Sport sport = new Sport();
        sport.setName(trimmedName);

        return sportRepository.save(sport);
    }

    public List<Trainer> getTrainers() {
        return trainerRepository.findAllByOrderByLastNameAscFirstNameAsc();
    }


    public Trainer deactivateTrainer(Long trainerId) {
        Trainer trainer = trainerRepository.findById(trainerId)
            .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));

        trainer.setActive(false);

        return trainerRepository.save(trainer);
    }
}
