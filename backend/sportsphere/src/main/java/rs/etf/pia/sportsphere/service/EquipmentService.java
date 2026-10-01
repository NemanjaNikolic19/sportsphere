package rs.etf.pia.sportsphere.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.dto.EquipmentOrderRequest;
import rs.etf.pia.sportsphere.dto.EquipmentRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Equipment;
import rs.etf.pia.sportsphere.entity.EquipmentOrder;
import rs.etf.pia.sportsphere.entity.EquipmentOrderItem;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.EquipmentOrderItemRepository;
import rs.etf.pia.sportsphere.repository.EquipmentOrderRepository;
import rs.etf.pia.sportsphere.repository.EquipmentRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.OrganizationEmployeeRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;

@Service
@Transactional
public class EquipmentService {
    
    private static final String ROLE_ATHLETE = "ATHLETE";
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private static final String FACILITY_STATUS_APPROVED = "APPROVED";

    private static final String ORDER_STATUS_ORDERED = "ORDERED";
    private static final String ORDER_STATUS_ACCEPTED = "ACCEPTED";
    private static final String ORDER_STATUS_PICKED_UP = "PICKED_UP";
    private static final String ORDER_STATUS_CANCELLED = "CANCELLED";

    private final AppUserRepository appUserRepository;
    private final FacilityRepository facilityRepository;
    private final OrganizationEmployeeRepository organizationEmployeeRepository;
    private final SportRepository sportRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentOrderRepository equipmentOrderRepository;
    private final EquipmentOrderItemRepository equipmentOrderItemRepository;
    
    public EquipmentService(AppUserRepository appUserRepository, FacilityRepository facilityRepository,
            OrganizationEmployeeRepository organizationEmployeeRepository, SportRepository sportRepository,
            EquipmentRepository equipmentRepository, EquipmentOrderRepository equipmentOrderRepository,
            EquipmentOrderItemRepository equipmentOrderItemRepository) {
        this.appUserRepository = appUserRepository;
        this.facilityRepository = facilityRepository;
        this.organizationEmployeeRepository = organizationEmployeeRepository;
        this.sportRepository = sportRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentOrderRepository = equipmentOrderRepository;
        this.equipmentOrderItemRepository = equipmentOrderItemRepository;
    }

    private AppUser getUserOrThrow(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private Equipment getEquipmentOrThrow(Long equipmentId) {
        return equipmentRepository.findById(equipmentId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment not found"));
    }

    private EquipmentOrder getEquipmentOrderOrThrow(Long equipmentOrderId){
        return equipmentOrderRepository.findById(equipmentOrderId)
                .orElseThrow(() -> new IllegalArgumentException("Equipment order not found"));
    }

    private void validateAthlete(AppUser athlete) {
        if(!ROLE_ATHLETE.equalsIgnoreCase(athlete.getRole())) {
            throw new IllegalArgumentException("User is not an athlete");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(athlete.getStatus())) {
            throw new IllegalArgumentException("Athlete account is not active");
        }
    }

    private void validateEmployee(AppUser employee) {
        if(!ROLE_EMPLOYEE.equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("User is not an employee");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalArgumentException("Employee account is not active");
        }
    }

    private List<Long> getEmployeeOrganizationIds(Long employeeId) {
        return organizationEmployeeRepository.findByEmployeeId(employeeId)
                .stream()
                .map(OrganizationEmployee::getOrganizationId)
                .toList();
    }

    private List<Long> getEmployeeFacilityIds(Long employeeId) {
        List<Long> organizationIds = getEmployeeOrganizationIds(employeeId);

        if(organizationIds.isEmpty()){
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

    private void validateEmployeeCanManageOrder(Long employeeId, EquipmentOrder order) {
        validateEmployeeCanManageFacility(employeeId, order.getFacilityId());
    }

    private void validateEquipmentRequest(EquipmentRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Equipment request required");
        }

        if(request.getFacilityId() == null) {
            throw new IllegalArgumentException("Facility required");
        }

        if(request.getSportId() == null) {
            throw new IllegalArgumentException("Sport required");
        }

        if(!sportRepository.existsById(request.getSportId())) {
            throw new IllegalArgumentException("Sport does not exist");
        }

        if(request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Equipment name is required");
        }

        if(request.getPrice() == null || request.getPrice().signum() < 0) {
            throw new IllegalArgumentException("Equipment price must be zero or positive");
        }

        if(request.getStock() == null || request.getStock() < 0) {
            throw new IllegalArgumentException("Equipment stock must be zero or positive");
        }
    }

    private void validateEquipmentOrderRequest(EquipmentOrderRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Equipment request required");
        }

        if(request.getFacilityId() == null) {
            throw new IllegalArgumentException("Facility required");
        }

        if(request.getItems() == null || request.getItems().isEmpty()) {
            throw new IllegalArgumentException("Order must contain at least one item");
        }

        Set<Long> equipmentIds = new HashSet<>();

        for(EquipmentOrderRequest.Item item : request.getItems()) {
            if(item.getEquipmentId() == null) {
                throw new IllegalArgumentException("Equipment id is required");
            }

            if(item.getQuantity() == null || item.getQuantity() <= 0) {
                throw new IllegalArgumentException("Quantity must be positive");
            }

            if(!equipmentIds.add(item.getEquipmentId())) {
                throw new IllegalArgumentException("Duplicate equipment item in order");
            }
        }
    }

    private boolean isOrderCancellable(EquipmentOrder order) {
        return ORDER_STATUS_ORDERED.equalsIgnoreCase(order.getStatus())
                || ORDER_STATUS_ACCEPTED.equalsIgnoreCase(order.getStatus());
    }

    private void restoreStock(EquipmentOrder order) {
        List<EquipmentOrderItem> items = equipmentOrderItemRepository.findByOrderId(order.getId());

        for(EquipmentOrderItem item : items) {
            Equipment equipment = getEquipmentOrThrow(item.getEquipmentId());

            equipment.setStock(equipment.getStock() + item.getQuantity());

            equipmentRepository.save(equipment);
        }
    }

    public List<Equipment> getEquipment(Long sportId) {
        if(sportId == null) {
            return equipmentRepository.findByActiveTrueOrderByNameAsc();
        }

        return equipmentRepository.findBySportIdAndActiveTrueOrderByNameAsc(sportId);
    }

    public List<Equipment> getEmployeeEquipment(Long employeeId) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        List<Long> facilityIds = getEmployeeFacilityIds(employeeId);

        if(facilityIds.isEmpty()){
            return List.of();
        }

        return equipmentRepository.findByFacilityIdInOrderByNameAsc(facilityIds);
    }

    public Equipment createEquipment(Long employeeId, EquipmentRequest request) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);
    
        validateEquipmentRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility.getId());

        Equipment equipment = new Equipment();

        equipment.setFacilityId(request.getFacilityId());
        equipment.setSportId(request.getSportId());
        equipment.setName(request.getName());
        equipment.setImagePath(request.getImagePath());
        equipment.setPrice(request.getPrice());
        equipment.setStock(request.getStock());

        if(request.getActive() == null) {
            equipment.setActive(true);
        } else {
            equipment.setActive(request.getActive());
        }

        return equipmentRepository.save(equipment);
    }

    public Equipment updateEquipment(Long employeeId, Long equipmentId, EquipmentRequest request) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        Equipment equipment = getEquipmentOrThrow(equipmentId);

        validateEmployeeCanManageFacility(employeeId, equipment.getFacilityId());

        validateEquipmentRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility.getId());
        
        equipment.setFacilityId(request.getFacilityId());
        equipment.setSportId(request.getSportId());
        equipment.setName(request.getName());
        equipment.setImagePath(request.getImagePath());
        equipment.setPrice(request.getPrice());
        equipment.setStock(request.getStock());

        if(request.getActive() != null) {
            equipment.setActive(request.getActive());
        }

        return equipmentRepository.save(equipment);
    }

    public EquipmentOrder createEquipmentOrder(Long athleteId, EquipmentOrderRequest request) {
        AppUser athlete = getUserOrThrow(athleteId);
        validateAthlete(athlete);

        validateEquipmentOrderRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());

        if(!FACILITY_STATUS_APPROVED.equalsIgnoreCase(facility.getStatus())) {
            throw new IllegalArgumentException("Facility is not approved");
        }

        BigDecimal totalPrice = BigDecimal.ZERO;

        List<EquipmentOrderItem> orderItems = new ArrayList<>();
        List<Equipment> orderedEquipments = new ArrayList<>();

        for(EquipmentOrderRequest.Item itemRequest : request.getItems()) {
            Equipment equipment = getEquipmentOrThrow(itemRequest.getEquipmentId());

            if(!Boolean.TRUE.equals(equipment.getActive())) {
                throw new IllegalArgumentException("Equipment is not active: " + equipment.getName());
            }

            if(!equipment.getFacilityId().equals(request.getFacilityId())) {
                throw new IllegalArgumentException("All equipment must belong to selected facility");
            }

            if(equipment.getStock() < itemRequest.getQuantity()) {
                throw new IllegalArgumentException("Not enough stock for equipment: " + equipment.getName());
            }

            equipment.setStock(equipment.getStock() - itemRequest.getQuantity());

            orderedEquipments.add(equipment);

            BigDecimal linePrice = equipment.getPrice()
                                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()));

            totalPrice = totalPrice.add(linePrice);

            EquipmentOrderItem orderItem = new EquipmentOrderItem();

            orderItem.setEquipmentId(equipment.getId());
            orderItem.setQuantity(itemRequest.getQuantity());
            orderItem.setUnitPrice(equipment.getPrice());

            orderItems.add(orderItem);
        }

        EquipmentOrder order = new EquipmentOrder();

        order.setAthleteId(athleteId);
        order.setFacilityId(request.getFacilityId());
        order.setStatus(ORDER_STATUS_ORDERED);
        order.setTotalPrice(totalPrice);

        EquipmentOrder savedOrder = equipmentOrderRepository.save(order);

        for(EquipmentOrderItem orderItem : orderItems) {
            orderItem.setOrderId(savedOrder.getId());
            equipmentOrderItemRepository.save(orderItem);
        }

        for(Equipment equipment : orderedEquipments) {
            equipmentRepository.save(equipment);
        }

        return savedOrder;
    }

    public List<EquipmentOrder> getAthleteOrders(Long userId) {
        AppUser athlete = getUserOrThrow(userId);
        validateAthlete(athlete);

        return equipmentOrderRepository.findByAthleteIdOrderByCreatedAtDesc(userId);
    }

    public EquipmentOrder cancelAthleteOrder(Long athleteId, Long orderId) {
        AppUser athlete = getUserOrThrow(athleteId);
        validateAthlete(athlete);

        EquipmentOrder order = getEquipmentOrderOrThrow(orderId);

        if(!order.getAthleteId().equals(athleteId)) {
            throw new IllegalArgumentException("Order does not belong to this athlete");
        }

        if(ORDER_STATUS_CANCELLED.equalsIgnoreCase(order.getStatus())) {
            return order;
        }

        if(!isOrderCancellable(order)) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }

        restoreStock(order);
        order.setStatus(ORDER_STATUS_CANCELLED);

        return equipmentOrderRepository.save(order);
    }

    public List<EquipmentOrder> getEmployeeOrders(Long employeeId) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        List<Long> facilityIds = getEmployeeFacilityIds(employeeId);

        if(facilityIds.isEmpty()) {
            return List.of();
        }

        return equipmentOrderRepository.findByFacilityIdInOrderByCreatedAtDesc(facilityIds);
    }

    public EquipmentOrder acceptOrder(Long employeeId, Long orderId) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        EquipmentOrder order = getEquipmentOrderOrThrow(orderId);
        validateEmployeeCanManageOrder(employeeId, order);

        if(!ORDER_STATUS_ORDERED.equalsIgnoreCase(order.getStatus())) {
            throw new IllegalArgumentException("Only ordered orders can be accepted");
        }

        order.setStatus(ORDER_STATUS_ACCEPTED);

        return equipmentOrderRepository.save(order);
    }

    public EquipmentOrder markOrderPickedUp(Long employeeId, Long orderId) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        EquipmentOrder order = getEquipmentOrderOrThrow(orderId);
        validateEmployeeCanManageOrder(employeeId, order);

        if(!ORDER_STATUS_ACCEPTED.equalsIgnoreCase(order.getStatus())) {
            throw new IllegalArgumentException("Only accepted orders can be picked up");
        }

        order.setStatus(ORDER_STATUS_PICKED_UP);

        return equipmentOrderRepository.save(order);
    }

    public EquipmentOrder cancelEmployeeOrder(Long employeeId, Long orderId) {
        AppUser employee = getUserOrThrow(employeeId);
        validateEmployee(employee);

        EquipmentOrder order = getEquipmentOrderOrThrow(orderId);
        validateEmployeeCanManageOrder(employeeId, order);

        if(ORDER_STATUS_CANCELLED.equalsIgnoreCase(order.getStatus())) {
            return order;
        }

        if(!isOrderCancellable(order)) {
            throw new IllegalArgumentException("Order cannot be cancelled");
        }

        restoreStock(order);

        order.setStatus(ORDER_STATUS_CANCELLED);

        return equipmentOrderRepository.save(order);
    }
}
