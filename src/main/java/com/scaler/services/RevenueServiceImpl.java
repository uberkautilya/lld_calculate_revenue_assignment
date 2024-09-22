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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
        Optional<User> userOptional = userRepository.findById(userId);
        if (userOptional.isPresent()) {
            UserType userType = userOptional.get().getUserType();
            if (!UserType.BILLING.equals(userType)) {
                throw new UnAuthorizedAccess("User doesn't have previleges to access this feature");
            }
        } else {
            throw new UserNotFoundException("User not found with userId: " + userId);
        }

        AggregatedRevenue aggregatedRevenue = null;
        List<DailyRevenue> revenues = new ArrayList<>();
        Date date = new Date();
//        CURRENT_FY, PREVIOUS_FY, CURRENT_MONTH, PREVIOUS_MONTH;

        switch (queryType) {
            case "CURRENT_MONTH" -> {
                revenues = revenueRepository.getDailyRevenueBetweenDates(date, date);
                aggregateRevenues(aggregatedRevenue, revenues, date, date);
                break;
            }
            case "PREVIOUS_MONTH" -> {

            }
            case "CURRENT_FY" -> {

            }
            case "PREVIOUS_FY" -> {

            }
        }
        return aggregatedRevenue;
    }

    private AggregatedRevenue aggregateRevenues(AggregatedRevenue aggregatedRevenue, List<DailyRevenue> revenues, Date fromDate, Date toDate) {
        aggregatedRevenue = new AggregatedRevenue();
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
