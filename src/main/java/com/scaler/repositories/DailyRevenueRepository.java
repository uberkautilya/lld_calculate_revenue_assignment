package com.scaler.repositories;

import com.scaler.models.DailyRevenue;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface DailyRevenueRepository extends JpaRepository<DailyRevenue, Integer> {
    public DailyRevenue save(DailyRevenue dailyRevenue);

    public List<DailyRevenue> getDailyRevenueBetweenDates(Date startDate, Date endDate);
}
