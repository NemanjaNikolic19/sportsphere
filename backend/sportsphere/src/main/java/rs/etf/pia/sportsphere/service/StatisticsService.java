package rs.etf.pia.sportsphere.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.dto.StatisticsResponse;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.EquipmentOrder;
import rs.etf.pia.sportsphere.entity.Reservation;
import rs.etf.pia.sportsphere.entity.Sport;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.EquipmentOrderRepository;
import rs.etf.pia.sportsphere.repository.ReservationRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;

@Service
@Transactional(readOnly = true)
public class StatisticsService {

    private static final String ROLE_ATHLETE =
        "ATHLETE";

    private static final String USER_STATUS_ACTIVE =
        "ACTIVE";

    private static final String RESERVATION_REQUESTED =
        "REQUESTED";

    private static final String RESERVATION_CONFIRMED =
        "CONFIRMED";

    private static final String ORDER_PICKED_UP =
        "PICKED_UP";

    private static final DateTimeFormatter
        MONTH_FORMATTER =
            DateTimeFormatter.ofPattern(
                "MMM yyyy",
                Locale.ENGLISH
            );

    private final ReservationRepository
        reservationRepository;

    private final SportRepository sportRepository;

    private final EquipmentOrderRepository
        equipmentOrderRepository;

    private final AppUserRepository appUserRepository;

    public StatisticsService(
            ReservationRepository reservationRepository,
            SportRepository sportRepository,
            EquipmentOrderRepository
                equipmentOrderRepository,
            AppUserRepository appUserRepository) {

        this.reservationRepository =
            reservationRepository;

        this.sportRepository = sportRepository;

        this.equipmentOrderRepository =
            equipmentOrderRepository;

        this.appUserRepository = appUserRepository;
    }

    public StatisticsResponse getStatistics(
            Long athleteId) {

        getActiveAthleteOrThrow(athleteId);

        LocalDateTime now = LocalDateTime.now();

        List<Reservation> reservations =
            reservationRepository
                .findByAthleteIdOrderByStartTimeDesc(
                    athleteId
                );

        List<Sport> sports =
            sportRepository.findAll()
                .stream()
                .sorted(
                    Comparator.comparing(
                        Sport::getName,
                        String.CASE_INSENSITIVE_ORDER
                    )
                )
                .toList();

        Map<String, Long> playedBySport =
            new LinkedHashMap<>();

        Map<String, Long> reservedBySport =
            new LinkedHashMap<>();

        Map<Long, String> sportNamesById =
            initializeSportMaps(
                sports,
                playedBySport,
                reservedBySport
            );

        calculateSportStatistics(
            reservations,
            now,
            sportNamesById,
            playedBySport,
            reservedBySport
        );

        Map<String, Long> monthlyActivity =
            calculateMonthlyActivity(
                reservations,
                now
            );

        Map<String, BigDecimal>
            equipmentSpendingByAthlete =
                calculateEquipmentSpending();

        BigDecimal totalEquipmentSpending =
            equipmentSpendingByAthlete
                .values()
                .stream()
                .reduce(
                    BigDecimal.ZERO,
                    BigDecimal::add
                );

        return new StatisticsResponse(
            playedBySport,
            reservedBySport,
            monthlyActivity,
            equipmentSpendingByAthlete,
            totalEquipmentSpending
        );
    }

    private Map<Long, String> initializeSportMaps(
            List<Sport> sports,
            Map<String, Long> playedBySport,
            Map<String, Long> reservedBySport) {

        Map<Long, String> sportNamesById =
            new HashMap<>();

        for(Sport sport : sports) {
            sportNamesById.put(
                sport.getId(),
                sport.getName()
            );

            playedBySport.put(
                sport.getName(),
                0L
            );

            reservedBySport.put(
                sport.getName(),
                0L
            );
        }

        return sportNamesById;
    }

    private void calculateSportStatistics(
            List<Reservation> reservations,
            LocalDateTime now,
            Map<Long, String> sportNamesById,
            Map<String, Long> playedBySport,
            Map<String, Long> reservedBySport) {

        for(Reservation reservation : reservations) {
            String sportName =
                sportNamesById.get(
                    reservation.getSportId()
                );

            if(sportName == null) {
                continue;
            }

            /*
             * A term is played only after it has ended
             * and only if it was confirmed.
             */
            if(
                RESERVATION_CONFIRMED.equals(
                    reservation.getStatus()
                ) &&
                reservation.getEndTime() != null &&
                !reservation.getEndTime().isAfter(now)
            ) {
                playedBySport.merge(
                    sportName,
                    1L,
                    Long::sum
                );

                continue;
            }

            /*
             * A future requested or confirmed term is
             * considered reserved.
             */
            if(
                isValidReservationStatus(
                    reservation.getStatus()
                ) &&
                reservation.getStartTime() != null &&
                reservation.getStartTime().isAfter(now)
            ) {
                reservedBySport.merge(
                    sportName,
                    1L,
                    Long::sum
                );
            }
        }
    }

    private Map<String, Long>
            calculateMonthlyActivity(
                List<Reservation> reservations,
                LocalDateTime now) {

        Map<String, Long> monthlyActivity =
            new LinkedHashMap<>();

        YearMonth currentMonth =
            YearMonth.from(now);

        YearMonth firstMonth =
            currentMonth.minusMonths(11);

        /*
         * Insert all 12 months first so months without
         * activity are also displayed on the chart.
         */
        for(int i = 0; i < 12; i++) {
            YearMonth month =
                firstMonth.plusMonths(i);

            monthlyActivity.put(
                formatMonth(month),
                0L
            );
        }

        LocalDateTime periodStart =
            firstMonth
                .atDay(1)
                .atStartOfDay();

        LocalDateTime periodEnd =
            currentMonth
                .plusMonths(1)
                .atDay(1)
                .atStartOfDay();

        for(Reservation reservation : reservations) {
            LocalDateTime startTime =
                reservation.getStartTime();

            if(
                startTime == null ||
                !isValidReservationStatus(
                    reservation.getStatus()
                ) ||
                startTime.isBefore(periodStart) ||
                !startTime.isBefore(periodEnd)
            ) {
                continue;
            }

            String month =
                formatMonth(
                    YearMonth.from(startTime)
                );

            monthlyActivity.merge(
                month,
                1L,
                Long::sum
            );
        }

        return monthlyActivity;
    }

    private Map<String, BigDecimal>
            calculateEquipmentSpending() {

        List<AppUser> athletes =
            appUserRepository
                .findByRoleAndStatus(
                    ROLE_ATHLETE,
                    USER_STATUS_ACTIVE
                )
                .stream()
                .sorted(
                    Comparator.comparing(
                        AppUser::getUsername,
                        String.CASE_INSENSITIVE_ORDER
                    )
                )
                .toList();

        Map<Long, String> usernamesById =
            new HashMap<>();

        Map<String, BigDecimal> spending =
            new LinkedHashMap<>();

        /*
         * Include athletes without completed purchases
         * so their chart value is zero.
         */
        for(AppUser athlete : athletes) {
            usernamesById.put(
                athlete.getId(),
                athlete.getUsername()
            );

            spending.put(
                athlete.getUsername(),
                BigDecimal.ZERO
            );
        }

        List<EquipmentOrder> orders =
            equipmentOrderRepository.findAll();

        for(EquipmentOrder order : orders) {
            if(!ORDER_PICKED_UP.equals(
                    order.getStatus())) {
                continue;
            }

            String username =
                usernamesById.get(
                    order.getAthleteId()
                );

            if(
                username == null ||
                order.getTotalPrice() == null
            ) {
                continue;
            }

            spending.merge(
                username,
                order.getTotalPrice(),
                BigDecimal::add
            );
        }

        return spending;
    }

    private boolean isValidReservationStatus(
            String status) {

        return (
            RESERVATION_REQUESTED.equals(status) ||
            RESERVATION_CONFIRMED.equals(status)
        );
    }

    private String formatMonth(
            YearMonth month) {

        return month.format(MONTH_FORMATTER);
    }

    private AppUser getActiveAthleteOrThrow(
            Long athleteId) {

        AppUser athlete =
            appUserRepository
                .findById(athleteId)
                .orElseThrow(() ->
                    new IllegalArgumentException(
                        "Athlete not found"
                    )
                );

        if(!ROLE_ATHLETE.equalsIgnoreCase(
                athlete.getRole())) {

            throw new IllegalArgumentException(
                "User is not an athlete"
            );
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(
                athlete.getStatus())) {

            throw new IllegalArgumentException(
                "Athlete account is not active"
            );
        }

        return athlete;
    }
}