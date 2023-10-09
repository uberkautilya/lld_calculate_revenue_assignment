# Calculate aggregated revenue for Restaurant Management System

## Problem Statement

You are building a Restaurant Management System. As a part of this system, you need to build a functionality using which users with billing privileges can generate aggregated revenue report for the restaurant.

## Assignment

Your task is to implement the following functionality.

#### Requirements

1. The revenue made by the restaurant is stored in the database in `daily_revenue` table.
    * This table has aggregated data for each day.
    * This table contains 4 columns:
        - id (primary key)
        - date (date on which the revenue was generated)
        - revenueFromFoodSales (revenue generated from food sales)
        - totalGst (total GST collected)
        - totalServiceCharge (total service charge collected)
2. Our functionality should support 4 types of queries:
    * Get revenue for current month
    * Get revenue for current financial year
    * Get revenue for previous month
    * Get revenue for previous financial year
3. The request for calculating revenue will contain user id and query type.
4. Depending upon the type of query the functionality should aggregate the relevant revenue data and return the response.
5. This functionality will only available to users with billing privileges.

#### Instructions

* Refer the `calculateRevenue` method inside `RevenueController` class.
* Refer the `CalculateRevenueRequestDto` and `CalculateRevenueResponseDto` for understanding the expected input and output to the functionality.
* Refer the models package to understand the models.
* Implement the `RevenueService`, `UserRepository` and `DailyRevenueRepository` interfaces to achieve the above requirements.
* We need in memory database implementation for this assignment.
* Refer the `TestRevenueController` class to understand the test cases that will be used to evaluate your solution.
* Do not modify existing methods and their parameters for interfaces, feel free to add more methods if required.