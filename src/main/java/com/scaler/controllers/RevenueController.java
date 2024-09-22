package com.scaler.controllers;

import com.scaler.dtos.CalculateRevenueRequestDto;
import com.scaler.dtos.CalculateRevenueResponseDto;
import com.scaler.dtos.ResponseStatus;
import com.scaler.exceptions.UnAuthorizedAccess;
import com.scaler.exceptions.UserNotFoundException;
import com.scaler.models.AggregatedRevenue;
import com.scaler.services.RevenueService;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RevenueController {

    private RevenueService revenueService;

    public RevenueController(RevenueService revenueService) {
        this.revenueService = revenueService;
    }

    @PostMapping("/")
    public CalculateRevenueResponseDto calculateRevenue(CalculateRevenueRequestDto requestDto) {
        try {
            AggregatedRevenue aggrRevenue = revenueService.calculateRevenue(requestDto.getUserId(), requestDto.getRevenueQueryType());
            CalculateRevenueResponseDto response = new CalculateRevenueResponseDto();
            response.setAggregatedRevenue(aggrRevenue);
            response.setResponseStatus(ResponseStatus.SUCCESS);
            return response;
        } catch (UnAuthorizedAccess e) {
            CalculateRevenueResponseDto response = new CalculateRevenueResponseDto();
            response.setResponseStatus(ResponseStatus.FAILURE);
            return response;
        } catch (UserNotFoundException e) {
            CalculateRevenueResponseDto response = new CalculateRevenueResponseDto();
            response.setResponseStatus(ResponseStatus.FAILURE);
            return response;
        }
    }
}
