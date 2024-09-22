package com.scaler.models;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import java.util.Date;

@Entity
@Table(name = "daily_revenue")
public class DailyRevenue extends BaseModel{
    private double revenueFromFoodSales;
    private Date date;
    private double totalGst;
    private double totalServiceCharge;

    public double getRevenueFromFoodSales() {
        return revenueFromFoodSales;
    }

    public void setRevenueFromFoodSales(double revenueFromFoodSales) {
        this.revenueFromFoodSales = revenueFromFoodSales;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public double getTotalGst() {
        return totalGst;
    }

    public void setTotalGst(double totalGst) {
        this.totalGst = totalGst;
    }

    public double getTotalServiceCharge() {
        return totalServiceCharge;
    }

    public void setTotalServiceCharge(double totalServiceCharge) {
        this.totalServiceCharge = totalServiceCharge;
    }
}
