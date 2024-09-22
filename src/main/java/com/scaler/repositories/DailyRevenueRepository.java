package com.scaler.repositories;

import com.scaler.models.DailyRevenue;

import java.util.Date;
import java.util.List;

public interface DailyRevenueRepository {
    public DailyRevenue save(DailyRevenue dailyRevenue);

    public List<DailyRevenue> getDailyRevenueBetweenDates(Date startDate, Date endDate);
}
