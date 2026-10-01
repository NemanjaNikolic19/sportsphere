package rs.etf.pia.sportsphere.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.dto.PromotionRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.entity.Promotion;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.OrganizationEmployeeRepository;
import rs.etf.pia.sportsphere.repository.PromotionRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;

@Service
@Transactional
public class PromotionService {
    
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private static final String DISCOUNT_TYPE_PERCENT = "PERCENT";
    private static final String DISCOUNT_TYPE_FIXED = "FIXED";


    private final AppUserRepository appUserRepository;
    private final FacilityRepository facilityRepository;
    private final OrganizationEmployeeRepository organizationEmployeeRepository;
    private final SportRepository sportRepository;
    private final PromotionRepository promotionRepository;

    public PromotionService(AppUserRepository appUserRepository, FacilityRepository facilityRepository,
            OrganizationEmployeeRepository organizationEmployeeRepository, SportRepository sportRepository,
            PromotionRepository promotionRepository) {
        this.appUserRepository = appUserRepository;
        this.facilityRepository = facilityRepository;
        this.organizationEmployeeRepository = organizationEmployeeRepository;
        this.sportRepository = sportRepository;
        this.promotionRepository = promotionRepository;
    }

    private AppUser getEmployeeOrThrow(Long employeeId) {
        return appUserRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private Promotion getPromotionOrThrow(Long promotionId) {
        return promotionRepository.findById(promotionId)
                .orElseThrow(() -> new IllegalArgumentException("Promotion not found"));
    }

    private void validateEmployee(AppUser employee) {
        if(!ROLE_EMPLOYEE.equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("User is not Employee");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalArgumentException("Employee account is not active");
        }
    }

    private List<Long> getEmployeeOrganizationIds(Long employeeId) {
        return organizationEmployeeRepository.findByEmployeeId(employeeId)
                .stream()
                .map(OrganizationEmployee::getOrganizationId)
                .distinct()
                .toList();
    }

    private List<Long> getEmployeeFacilityIds(Long employeeId) {
        List<Long> organizationIds = getEmployeeOrganizationIds(employeeId);

        if(organizationIds.isEmpty()) {
            return List.of();
        }

        return facilityRepository.findByOrganizationIdIn(organizationIds)
                .stream()
                .map(Facility::getId)
                .toList();
    }

    private void validateEmployeeCanManageFacility(Long employeeId, Long facilityId) {
        List<Long> facilityIds = getEmployeeFacilityIds(employeeId);

        if(!facilityIds.contains(facilityId)) {
            throw new IllegalArgumentException("Employee cannot manage this facility");
        }
    }

    private String normalizeDiscountType(String discountType) {
        if(discountType == null) {
            return null;
        }

        return discountType.trim().toUpperCase();
    }

    private void validatePromotionRequest(PromotionRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Request is required");
        }

        if(request.getFacilityId() == null) {
            throw new IllegalArgumentException("Facility is required");
        }

        if(request.getSportId() != null &&
            !sportRepository.existsById(request.getSportId())
        ) {
            throw new IllegalArgumentException("Sport does not exist");
        }

        if(request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Promotion name is required");
        }

        if(request.getValidFrom() == null || request.getValidTo() == null) {
            throw new IllegalArgumentException("Promotion validity period is required");
        }

        if(request.getValidTo().isBefore(request.getValidFrom())) {
            throw new IllegalArgumentException("Valid to must be same date or after valid from");
        }

        if(request.getDiscountType() == null) {
            throw new IllegalArgumentException("Promotion discount type is required");
        }

        String discountType = normalizeDiscountType(request.getDiscountType());

        if(!DISCOUNT_TYPE_FIXED.equals(discountType) &&
            !DISCOUNT_TYPE_PERCENT.equals(discountType)
        ) {
            throw new IllegalArgumentException("Discount type must be PERCENT or FIXED");
        }

        if(request.getDiscountValue() == null || request.getDiscountValue().signum() < 0) {
            throw new IllegalArgumentException("Discount value must be zero or positive");
        }

        if(DISCOUNT_TYPE_PERCENT.equals(discountType) &&
            request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0
        ) {
            throw new IllegalArgumentException("Percent discount cannot be greater that 100");
        }
    }


    public List<Promotion> getActivePromotions() {
        return promotionRepository.findActivePromotions(LocalDate.now());
    }

    public List<Promotion> getUpcomingPromotions() {
        return promotionRepository.findUpcomingPromotions(LocalDate.now());
    }

    public List<Promotion> getEmployeePromotions(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        List<Long> facilityIds = getEmployeeFacilityIds(employeeId);

        if(facilityIds.isEmpty()) {
            return List.of();
        }

        return promotionRepository.findByFacilityIdInOrderByValidFromDesc(facilityIds);
    }

    public Promotion createPromotion(Long employeeId, PromotionRequest request) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        validatePromotionRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility.getId());

        Promotion promotion = new Promotion();

        promotion.setFacilityId(request.getFacilityId());
        promotion.setSportId(request.getSportId());
        promotion.setName(request.getName());
        promotion.setValidFrom(request.getValidFrom());
        promotion.setValidTo(request.getValidTo());
        promotion.setDiscountType(normalizeDiscountType(request.getDiscountType()));
        promotion.setDiscountValue(request.getDiscountValue());

        if(request.getActive() == null) {
            promotion.setActive(true);
        } else {
            promotion.setActive(request.getActive());
        }

        return promotionRepository.save(promotion);
    }

    public Promotion updatePromotion(Long employeeId, Long promotionId, PromotionRequest request) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        Promotion promotion = getPromotionOrThrow(promotionId);

        validateEmployeeCanManageFacility(employeeId, promotion.getFacilityId());

        validatePromotionRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility.getId());

        promotion.setFacilityId(request.getFacilityId());
        promotion.setSportId(request.getSportId());
        promotion.setName(request.getName());
        promotion.setValidFrom(request.getValidFrom());
        promotion.setValidTo(request.getValidTo());
        promotion.setDiscountType(normalizeDiscountType(request.getDiscountType()));
        promotion.setDiscountValue(request.getDiscountValue());

        if(request.getActive() != null) {
            promotion.setActive(request.getActive());
        }

        return promotionRepository.save(promotion);
    }
}
