package com.scaler.repositories;

import com.scaler.models.DailyRevenue;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class DailyRevenueRepositoryImpl implements DailyRevenueRepository {
    List<DailyRevenue> dailyRevenues = new ArrayList<>();
    @Override
    public DailyRevenue save(DailyRevenue dailyRevenue) {
        return dailyRevenues.stream()
                .filter(revenue -> revenue.getId() == dailyRevenue.getId())
                .findFirst().orElse(null);
    }

    @Override
    public List<DailyRevenue> getDailyRevenueBetweenDates(Date startDate, Date endDate) {
        return dailyRevenues.stream()
                .filter(revenue -> revenue.getDate().after(startDate) && revenue.getDate().before(endDate))
                .collect(Collectors.toList());
    }
}
