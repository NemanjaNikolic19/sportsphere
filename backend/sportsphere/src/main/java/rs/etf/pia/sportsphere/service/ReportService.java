package rs.etf.pia.sportsphere.service;

import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.openpdf.text.Document;
import org.openpdf.text.DocumentException;
import org.openpdf.text.Element;
import org.openpdf.text.Font;
import org.openpdf.text.FontFactory;
import org.openpdf.text.PageSize;
import org.openpdf.text.Paragraph;
import org.openpdf.text.Phrase;
import org.openpdf.text.pdf.PdfPCell;
import org.openpdf.text.pdf.PdfPTable;
import org.openpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Equipment;
import rs.etf.pia.sportsphere.entity.EquipmentOrder;
import rs.etf.pia.sportsphere.entity.EquipmentOrderItem;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.entity.Reservation;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.EquipmentOrderItemRepository;
import rs.etf.pia.sportsphere.repository.EquipmentOrderRepository;
import rs.etf.pia.sportsphere.repository.EquipmentRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.OrganizationEmployeeRepository;
import rs.etf.pia.sportsphere.repository.ReservationRepository;
import rs.etf.pia.sportsphere.repository.ResourceRepository;

@Service
@Transactional(readOnly = true)
public class ReportService {
    
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private final AppUserRepository appUserRepository;
    private final OrganizationEmployeeRepository organizationEmployeeRepository;
    private final FacilityRepository facilityRepository;
    private final ResourceRepository resourceRepository;
    private final ReservationRepository reservationRepository;
    private final EquipmentRepository equipmentRepository;
    private final EquipmentOrderRepository equipmentOrderRepository;
    private final EquipmentOrderItemRepository equipmentOrderItemRepository;
    public ReportService(AppUserRepository appUserRepository,
            OrganizationEmployeeRepository organizationEmployeeRepository, FacilityRepository facilityRepository,
            ResourceRepository resourceRepository, ReservationRepository reservationRepository,
            EquipmentRepository equipmentRepository, EquipmentOrderRepository equipmentOrderRepository,
            EquipmentOrderItemRepository equipmentOrderItemRepository) {
        this.appUserRepository = appUserRepository;
        this.organizationEmployeeRepository = organizationEmployeeRepository;
        this.facilityRepository = facilityRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.equipmentRepository = equipmentRepository;
        this.equipmentOrderRepository = equipmentOrderRepository;
        this.equipmentOrderItemRepository = equipmentOrderItemRepository;
    }

    private static class EquipmentTurnover {
        private int quantity;
        private BigDecimal revenue;

        private EquipmentTurnover() {
            this.quantity = 0;
            this.revenue = BigDecimal.ZERO;
        }

        private void add(int quantity, BigDecimal revenue) {
            this.quantity += quantity;
            this.revenue = this.revenue.add(revenue);
        }
    }

    private AppUser getEmployeeOrThrow(Long employeeId) {
        return appUserRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }
    private void validateEmployee(AppUser employee){
        if(!ROLE_EMPLOYEE.equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("User is not an employee");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalArgumentException("Employee account is not active");
        }
    }

    private void validateYearAndMonth(int year, int month) {
        if(year < 2000 || year > 2100) {
            throw new IllegalArgumentException("Invalid year");
        }

        if(month < 1 || month > 12) {
            throw new IllegalArgumentException("Invalid month");
        }
    }

    private List<Long> getEmployeeOrganizationIds(Long employeeId){
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

    private LocalDateTime max(LocalDateTime a, LocalDateTime b) {
        return a.isAfter(b) ? a: b;
    }

    private LocalDateTime min(LocalDateTime a, LocalDateTime b) {
        return a.isBefore(b) ? a: b;
    }

    private String formatHours(long minutes) {
        return String.format(Locale.US, "%.2f", minutes/60.0);
    }

    private String formatMoney(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private PdfPCell headerCell(String text, Font font) {
        PdfPCell cell =  new PdfPCell(new Phrase(text, font));
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        return cell;
    }

    private PdfPCell normalCell(String text, Font font) {
        return new PdfPCell(new Phrase(text, font));
    }

    private void addOccupancyTable(
        Document document,
        List<Resource> resources,
        Map<Long, Facility> facilityById,
        Map<Long, Long> reservedMinutesByResourceId,
        YearMonth yearMonth,
        Font headerFont,
        Font normalFont
    ) throws DocumentException {
        Paragraph heading = new Paragraph("Resource occupancy", headerFont);
        heading.setSpacingBefore(15);
        heading.setSpacingAfter(8);
        document.add(heading);

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.2f, 2.5f, 2.5f, 1.5f, 1.5f, 1.5f});

        table.addCell(headerCell("Resource ID", headerFont));
        table.addCell(headerCell("Facility", headerFont));
        table.addCell(headerCell("Resource", headerFont));
        table.addCell(headerCell("Reserved h", headerFont));
        table.addCell(headerCell("Available h", headerFont));
        table.addCell(headerCell("Occupancy %", headerFont));

        if(resources.isEmpty()) {
            PdfPCell emptyCell = normalCell("No active resources found", normalFont);
            emptyCell.setColspan(6);
            table.addCell(emptyCell);
        }

        for(Resource resource : resources) {
            Facility facility = facilityById.get(resource.getFacilityId());

            long reservedMinutes = reservedMinutesByResourceId.getOrDefault(resource.getId(), 0L);

            long workingMinutesPerDay = 24L * 60L;

            if(facility != null &&
                facility.getWorkingTimeFrom() != null &&
                facility.getWorkingTimeTo() != null
            ) {
                long calculated = Duration.between(
                                        facility.getWorkingTimeFrom(), 
                                        facility.getWorkingTimeTo()
                                    ).toMinutes();

                if(calculated > 0) {
                    workingMinutesPerDay = calculated;
                }
            }

            long availableMinutes = workingMinutesPerDay * yearMonth.lengthOfMonth();

            BigDecimal occupancyPercent = BigDecimal.ZERO;

            if(availableMinutes > 0) {
                occupancyPercent = BigDecimal.valueOf(reservedMinutes)
                                    .multiply(BigDecimal.valueOf(100))
                                    .divide(BigDecimal.valueOf(availableMinutes),2,RoundingMode.HALF_UP);
            }

            table.addCell(normalCell(String.valueOf(resource.getId()), normalFont));
            table.addCell(normalCell(facility == null ? "-" : facility.getName(), normalFont));
            table.addCell(normalCell(resource.getName(), normalFont));
            table.addCell(normalCell(formatHours(reservedMinutes), normalFont));
            table.addCell(normalCell(formatHours(availableMinutes), normalFont));
            table.addCell(normalCell(occupancyPercent.toPlainString(), normalFont));
        }

        document.add(table);
    }

    private void addEquipmentTurnoverTable(
        Document document,
        List<EquipmentOrder> orders,
        List<EquipmentOrderItem> items,
        Map<Long, Equipment> equipmentById,
        Font headerFont,
        Font normalFont
    ) throws DocumentException {
        Paragraph heading = new Paragraph("Equipment turnover", headerFont);
        heading.setSpacingBefore(15);
        heading.setSpacingAfter(8);
        document.add(heading);

        Map<Long, EquipmentTurnover> turnoverByEquipmentId = new HashMap<>();

        for(EquipmentOrderItem item : items) {
            BigDecimal revenue = item.getUnitPrice()
                                    .multiply(BigDecimal.valueOf(item.getQuantity()));

            turnoverByEquipmentId
                .computeIfAbsent(item.getEquipmentId(), key -> new EquipmentTurnover())
                .add(item.getQuantity(), revenue);
        }

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 1.2f, 4.0f, 1.5f, 2.0f });

        table.addCell(headerCell("Equipment Id", headerFont));
        table.addCell(headerCell("Equipment", headerFont));
        table.addCell(headerCell("Quantity", headerFont));
        table.addCell(headerCell("Revenue", headerFont));
    
        if(orders.isEmpty() || turnoverByEquipmentId.isEmpty()) {
            PdfPCell emptyCell = normalCell("No equipment turnover found for selected month.", normalFont);
            emptyCell.setColspan(4);
            table.addCell(emptyCell);
        }

        for(Map.Entry<Long, EquipmentTurnover> entry : turnoverByEquipmentId.entrySet()) {
            Long equipmentId = entry.getKey();
            EquipmentTurnover turnover = entry.getValue();
            Equipment equipment = equipmentById.get(equipmentId);

            table.addCell(normalCell(String.valueOf(equipmentId), normalFont));
            table.addCell(normalCell(equipment == null ? "-" : equipment.getName(), normalFont));
            table.addCell(normalCell(String.valueOf(turnover.quantity), normalFont));
            table.addCell(normalCell(formatMoney(turnover.revenue), normalFont));
        }

        document.add(table);
    }

    public byte[] generateMonthlyReport(Long employeeId, int year, int month) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);
        validateYearAndMonth(year, month);

        List<Long> facilityIds = getEmployeeFacilityIds(employeeId);

        if(facilityIds.isEmpty()) {
            throw new IllegalArgumentException("Employee has no facilities");
        }

        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime periodStart = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime periodEnd = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        List<Facility> facilities = facilityRepository.findAllById(facilityIds);

        Map<Long, Facility> facilityById = facilities.stream()
                .collect(Collectors.toMap(Facility::getId, facility -> facility));

        List<Resource> resources = resourceRepository
                .findByFacilityIdInAndActiveTrueOrderByFacilityIdAscNameAsc(facilityIds);

        List<Reservation> reservations = reservationRepository.findMonthlyReportReservations(
                facilityIds, 
                periodStart, 
                periodEnd
        );

        Map<Long, Long> reservedMinutesByResourceId = new HashMap<>();

        for(Reservation reservation : reservations) {
            LocalDateTime start = max(reservation.getStartTime(), periodStart);
            LocalDateTime end = min(reservation.getEndTime(), periodEnd);

            long minutes = Duration.between(start, end).toMinutes();

            if (minutes > 0) {
                reservedMinutesByResourceId.merge(
                    reservation.getResourceId(), 
                    minutes, 
                    Long::sum
                );
            }
        }

        List<EquipmentOrder> orders = equipmentOrderRepository.findMonthlyReportOrders(
                facilityIds, 
                periodStart, 
                periodEnd
        );

        List<Long> orderIds = orders.stream()
                .map(EquipmentOrder::getId)
                .toList();

        List<EquipmentOrderItem> items = orderIds.isEmpty()
                ? List.of()
                : equipmentOrderItemRepository.findByOrderIdIn(orderIds);

        Set<Long> equipmentIds = items.stream()
                .map(EquipmentOrderItem::getEquipmentId)
                .collect(Collectors.toSet());

        List<Equipment> equipmentList = equipmentIds.isEmpty()
                ? List.of()
                : equipmentRepository.findAllById(equipmentIds);

        Map<Long, Equipment> equipmentById = equipmentList.stream()
                .collect(Collectors.toMap(Equipment::getId, equipment -> equipment));

        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, outputStream);

            Font titleFont = FontFactory.getFont("Helvetica", 18, Font.BOLD);
            Font headerFont = FontFactory.getFont("Helvetica", 10, Font.BOLD);
            Font normalFont = FontFactory.getFont("Helvetica", 10, Font.NORMAL);


            document.open();

            Paragraph title = new Paragraph("Sportsphere Monthly Report", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(15);
            document.add(title);

            document.add(new Paragraph("Employee ID: " + employeeId, normalFont));
            document.add(new Paragraph("Report month: " + yearMonth, normalFont));
            document.add(new Paragraph("Facilities: " + facilityIds, normalFont));

            addOccupancyTable(
                document, 
                resources, 
                facilityById, 
                reservedMinutesByResourceId, 
                yearMonth, 
                headerFont, 
                normalFont
            );

            addEquipmentTurnoverTable(
                document, 
                orders, 
                items, 
                equipmentById, 
                headerFont, 
                normalFont
            );

            document.close();

            return outputStream.toByteArray();
        } catch(DocumentException e) {
            throw new IllegalStateException("Could not generate PDF report", e);
        }
    }
}
