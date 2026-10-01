package rs.etf.pia.sportsphere.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import rs.etf.pia.sportsphere.dto.TeammateAdRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.TeammateAd;
import rs.etf.pia.sportsphere.entity.TeammateRequest;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;
import rs.etf.pia.sportsphere.repository.TeammateAdRepository;
import rs.etf.pia.sportsphere.repository.TeammateRequestRepository;

@Service
@Transactional
public class TeammateAdService {
    
    private static final String ROLE_ATHLETE = "ATHLETE";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private static final String AD_STATUS_ACTIVE = "ACTIVE";
    private static final String AD_STATUS_COMPLETED = "COMPLETED";
    private static final String AD_STATUS_CLOSED = "CLOSED";
    
    private static final String REQUEST_STATUS_PENDING = "PENDING";
    private static final String REQUEST_STATUS_ACCEPTED = "ACCEPTED";
    private static final String REQUEST_STATUS_REJECTED = "REJECTED";

    private final AppUserRepository appUserRepository;
    private final SportRepository sportRepository;
    private final TeammateAdRepository teammateAdRepository;
    private final TeammateRequestRepository teammateRequestRepository;

    public TeammateAdService(AppUserRepository appUserRepository, SportRepository sportRepository,
            TeammateAdRepository teammateAdRepository, TeammateRequestRepository teammateRequestRepository) {
        this.appUserRepository = appUserRepository;
        this.sportRepository = sportRepository;
        this.teammateAdRepository = teammateAdRepository;
        this.teammateRequestRepository = teammateRequestRepository;
    }

    private AppUser getAthleteOrThrow(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));
    }

    private TeammateAd getTeammateAdOrThrow(Long adId) {
        return teammateAdRepository.findById(adId)
                .orElseThrow(() -> new IllegalArgumentException("Teammate ad not found"));
    }

    private TeammateRequest getTeammateRequestOrThrow(Long requestId) {
        return teammateRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Teammate request not found"));
    }

    private void validateAthlete(AppUser athlete) {
        if(!ROLE_ATHLETE.equalsIgnoreCase(athlete.getRole())) {
            throw new IllegalArgumentException("User is not athlete");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(athlete.getStatus())) {
            throw new IllegalArgumentException("Athlete account is not active");
        }
    }

    private void validateAdRequest(TeammateAdRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Teammate ad request is required");
        }

        if(request.getSportId() == null) {
            throw new IllegalArgumentException("Sport is required");
        }

        if(!sportRepository.existsById(request.getSportId())) {
            throw new IllegalArgumentException("Sport does not exist");
        }

        if(request.getCity() == null) {
            throw new IllegalArgumentException("City is required");
        }

        if(request.getPlayDate() == null) {
            throw new IllegalArgumentException("Play date is required");
        }

        if(request.getStartTime() == null || request.getEndTime() == null) {
            throw new IllegalArgumentException("Start and end time are required");
        }

        if(!request.getStartTime().isBefore(request.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        LocalDateTime playStart = LocalDateTime.of(request.getPlayDate(), request.getStartTime());

        if(playStart.isBefore(LocalDateTime.now())){
            throw new IllegalArgumentException("Play time cannot be in the past");
        }

        if(request.getNeededPlayers() == null || request.getNeededPlayers() <= 0) {
            throw new IllegalArgumentException("Needed players must be positive");
        }
    }

    private void validateAdOwner(Long userId, TeammateAd ad) {
        if(!ad.getAthleteId().equals(userId)){
            throw new IllegalArgumentException("Only ad owner can perform this action");
        }
    }

    private int getAcceptedPlayers(TeammateAd ad){
        if(ad.getAcceptedPlayers() == null){
            return 0;
        }

        return ad.getAcceptedPlayers();
    }

    private boolean isAdFull(TeammateAd ad) {
        return getAcceptedPlayers(ad) >= ad.getNeededPlayers();
    }
    
    public List<TeammateAd> getActiveAds() {
        return teammateAdRepository.findByStatusOrderByPlayDateAscStartTimeAsc(AD_STATUS_ACTIVE);
    }

    public TeammateAd createAd(Long userId, TeammateAdRequest request) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        validateAdRequest(request);

        TeammateAd ad = new TeammateAd();

        ad.setAthleteId(userId);
        ad.setSportId(request.getSportId());
        ad.setCity(request.getCity());
        ad.setPlayDate(request.getPlayDate());
        ad.setStartTime(request.getStartTime());
        ad.setEndTime(request.getEndTime());
        ad.setNeededPlayers(request.getNeededPlayers());
        ad.setAcceptedPlayers(0);
        ad.setStatus(AD_STATUS_ACTIVE);

        return teammateAdRepository.save(ad);
    }

    public TeammateRequest joinAd(Long userId, Long adId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        TeammateAd ad = getTeammateAdOrThrow(adId);

        if(!AD_STATUS_ACTIVE.equalsIgnoreCase(ad.getStatus())) {
            throw new IllegalArgumentException("Ad is not active");
        }

        if(ad.getAthleteId().equals(userId)) {
            throw new IllegalArgumentException("Ad owner cannot join own ad");
        }

        if(isAdFull(ad)) {
            ad.setStatus(AD_STATUS_COMPLETED);
            teammateAdRepository.save(ad);
            throw new IllegalArgumentException("Ad is already full");
        }

        if(teammateRequestRepository.existsByAdIdAndAthleteId(adId, userId)) {
            throw new IllegalArgumentException("Athlete has already sent request for this ad");
        }

        TeammateRequest request = new TeammateRequest();

        request.setAdId(adId);
        request.setAthleteId(userId);
        request.setStatus(REQUEST_STATUS_PENDING);

        return teammateRequestRepository.save(request);
    }

    public List<TeammateRequest> getRequestsForOwnerAds(Long userId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        return teammateRequestRepository.findRequestsForOwnerAds(userId);
    }

    public TeammateRequest approveRequest(Long userId, Long requestId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        TeammateRequest request = getTeammateRequestOrThrow(requestId);
        TeammateAd ad = getTeammateAdOrThrow(request.getAdId());

        validateAdOwner(userId, ad);

        if(!AD_STATUS_ACTIVE.equalsIgnoreCase(ad.getStatus())) {
            throw new IllegalArgumentException("Ad is not active");
        }

        if(!REQUEST_STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be approved");
        }

        if(isAdFull(ad)) {
            ad.setStatus(AD_STATUS_COMPLETED);
            teammateAdRepository.save(ad);
            throw new IllegalArgumentException("Ad is already full");
        }

        request.setStatus(REQUEST_STATUS_ACCEPTED);

        ad.setAcceptedPlayers(getAcceptedPlayers(ad) + 1);

        if (isAdFull(ad)) {
            ad.setStatus(AD_STATUS_COMPLETED);
        }

        teammateAdRepository.save(ad);

        return teammateRequestRepository.save(request);
    }

    public TeammateRequest rejectRequest(Long userId, Long requestId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        TeammateRequest request = getTeammateRequestOrThrow(requestId);
        TeammateAd ad = getTeammateAdOrThrow(request.getAdId());

        validateAdOwner(userId, ad);

        if(!REQUEST_STATUS_PENDING.equalsIgnoreCase(request.getStatus())) {
            throw new IllegalArgumentException("Only pending requests can be rejected");
        }

        request.setStatus(REQUEST_STATUS_REJECTED);

        return teammateRequestRepository.save(request);
    }

    public TeammateAd closeAd(Long userId, Long adId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        TeammateAd ad = getTeammateAdOrThrow(adId);
        
        validateAdOwner(userId, ad);

        if(AD_STATUS_CLOSED.equalsIgnoreCase(ad.getStatus())) {
            return ad;
        }

        ad.setStatus(AD_STATUS_CLOSED);

        return teammateAdRepository.save(ad);
    }
}
