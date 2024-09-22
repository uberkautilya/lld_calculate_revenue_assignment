package com.scaler.services;

import com.scaler.exceptions.UnAuthorizedAccess;
import com.scaler.exceptions.UserNotFoundException;
import com.scaler.models.AggregatedRevenue;
import com.scaler.models.DailyRevenue;
import com.scaler.models.User;
import com.scaler.models.UserType;
import com.scaler.repositories.DailyRevenueRepository;
import com.scaler.repositories.UserRepository;
import org.springframework.stereotype.Service;

import java.time.*;
import java.util.*;

@Service
public class RevenueServiceImpl implements RevenueService {
    DailyRevenueRepository revenueRepository;
    UserRepository userRepository;

    public RevenueServiceImpl(DailyRevenueRepository dailyRevenueRepository, UserRepository userRepository) {
        this.revenueRepository = dailyRevenueRepository;
        this.userRepository = userRepository;
    }

    @Override
    public AggregatedRevenue calculateRevenue(long userId, String queryType) throws UnAuthorizedAccess, UserNotFoundException {
        validateUserPrivilege(userId);

        AggregatedRevenue aggregatedRevenue = new AggregatedRevenue();
        List<DailyRevenue> revenues = new ArrayList<>();
        Date date = new Date();

        switch (queryType) {
            case "CURRENT_MONTH" -> {
                aggregateRevenues(aggregatedRevenue, currentMonth());
                break;
            }
            case "PREVIOUS_MONTH" -> {
                aggregateRevenues(aggregatedRevenue, previousMonth());
                break;
            }
            case "CURRENT_FY" -> {
                aggregateRevenues(aggregatedRevenue, currentFY());
                break;
            }
            case "PREVIOUS_FY" -> {
                aggregateRevenues(aggregatedRevenue, previousFY());
                break;
            }
        }
        return aggregatedRevenue;
    }

    private void validateUserPrivilege(long userId) throws UnAuthorizedAccess, UserNotFoundException {
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserType userType = userOptional.get().getUserType();
            if (!UserType.BILLING.equals(userType)) {
                throw new UnAuthorizedAccess("User doesn't have previleges to access this feature");
            }
        } else {
            throw new UserNotFoundException("User not found with userId: " + userId);
        }
    }

    private Map<String, Date> previousFY() {
        Map<String, Date> queryDates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        int startYear = now.getYear();
        int endYear = now.getYear();
        Month startMonth = now.getMonth();
        Month endMonth = Month.MARCH;

        if (startMonth == Month.JANUARY || startMonth == Month.FEBRUARY || startMonth == Month.MARCH) {
            startYear = startYear - 1;
        } else {
            endYear = startYear + 1;
        }
        startMonth = Month.APRIL;
        startYear = startYear - 1;
        endYear = endYear - 1;
        boolean isEndYearLY = Year.isLeap(endYear);

        LocalDateTime start = LocalDateTime.of(startYear, startMonth.getValue(), 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(endYear, endMonth, Month.MARCH.length(isEndYearLY), 23, 59, 59);
        queryDates.put("FROM", Date.from(start.toInstant(ZoneOffset.of("IST"))));
        queryDates.put("TO", Date.from(to.toInstant(ZoneOffset.of("IST"))));
        return queryDates;
    }

    private Map<String, Date> currentFY() {
        Map<String, Date> queryDates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        int startYear = now.getYear();
        int endYear = now.getYear();
        Month startMonth = now.getMonth();
        Month endMonth = Month.MARCH;

        if (startMonth == Month.JANUARY || startMonth == Month.FEBRUARY || startMonth == Month.MARCH) {
            startYear = startYear - 1;
        } else {
            endYear = startYear + 1;
        }
        startMonth = Month.APRIL;
        boolean isEndYearLY = Year.isLeap(endYear);

        LocalDateTime start = LocalDateTime.of(startYear, startMonth.getValue(), 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(endYear, endMonth, Month.MARCH.length(isEndYearLY), 23, 59, 59);
        queryDates.put("FROM", Date.from(start.toInstant(ZoneOffset.of("IST"))));
        queryDates.put("TO", Date.from(to.toInstant(ZoneOffset.of("IST"))));
        return queryDates;
    }

    private Map<String, Date> previousMonth() {
        Map<String, Date> queryDates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime prev = now.minusMonths(1L);
        int prevYear = prev.getYear();
        Month prevMonth = prev.getMonth();
        boolean isPrevLeapYear = Year.isLeap(prevYear);
        int daysInPrevMonth = prevMonth.length(isPrevLeapYear);

        LocalDateTime start = LocalDateTime.of(prevYear, prevMonth.getValue(), 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(prevYear, prevMonth.getValue(), daysInPrevMonth, 23, 59, 59);
        queryDates.put("FROM", Date.from(start.toInstant(ZoneOffset.of("IST"))));
        queryDates.put("TO", Date.from(to.toInstant(ZoneOffset.of("IST"))));
        return queryDates;
    }

    private Map<String, Date> currentMonth() {
        Map<String, Date> queryDates = new HashMap<>();
        LocalDateTime now = LocalDateTime.now();

        int year = now.getYear();
        Month currentMonth = now.getMonth();
        boolean isLeapYear = Year.isLeap(year);
        int daysInThisMonth = currentMonth.length(isLeapYear);

        LocalDateTime start = LocalDateTime.of(year, currentMonth.getValue(), 1, 0, 0, 0);
        LocalDateTime to = LocalDateTime.of(year, currentMonth.getValue(), daysInThisMonth, 23, 59, 59);
        queryDates.put("FROM", Date.from(start.toInstant(ZoneOffset.of("IST"))));
        queryDates.put("TO", Date.from(to.toInstant(ZoneOffset.of("IST"))));
        return queryDates;
    }

    private AggregatedRevenue aggregateRevenues(AggregatedRevenue aggregatedRevenue, Map<String, Date> dateMap) {
        Date fromDate = dateMap.get("FROM");
        Date toDate = dateMap.get("TO");
        List<DailyRevenue> revenues = revenueRepository.getDailyRevenueBetweenDates(fromDate, toDate);

        aggregatedRevenue.setFromDate(fromDate);
        aggregatedRevenue.setToDate(toDate);
        aggregatedRevenue.setRevenueFromFoodSales(revenues.stream()
                .map(DailyRevenue::getRevenueFromFoodSales)
                .reduce(0.0, Double::sum));
        aggregatedRevenue.setTotalServiceCharge(revenues.stream()
                .map(DailyRevenue::getTotalServiceCharge)
                .reduce(0.0, Double::sum));
        aggregatedRevenue.setTotalGst(revenues.stream()
                .map(DailyRevenue::getTotalGst)
                .reduce(0.0, Double::sum));
        aggregatedRevenue.setTotalRevenue(aggregatedRevenue.getRevenueFromFoodSales()
                - aggregatedRevenue.getTotalGst()
                - aggregatedRevenue.getTotalServiceCharge());
        return aggregatedRevenue;
    }
}
