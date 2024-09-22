package com.scaler.controllers;

import com.scaler.dtos.CalculateRevenueRequestDto;
import com.scaler.dtos.CalculateRevenueResponseDto;
import com.scaler.dtos.ResponseStatus;
import com.scaler.models.DailyRevenue;
import com.scaler.models.User;
import com.scaler.models.UserType;
import com.scaler.repositories.DailyRevenueRepository;
import com.scaler.repositories.UserRepository;
import com.scaler.services.RevenueService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import java.lang.reflect.Constructor;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class TestRevenueControllers {

    private DailyRevenueRepository dailyRevenueRepository;
    private UserRepository userRepository;
    private RevenueService revenueService;
    private RevenueController revenueController;

    @BeforeEach
    public void setupTest() throws Exception {
        initializeComponents();
    }

    private void initializeComponents() throws Exception {
        initializeRepositories();
        initializeRevenueService();
        initializeRevenueController();
    }

    private <T> T createInstance(Class<T> interfaceClass, Reflections reflections) throws Exception {
        Set<Class<? extends T>> implementations = reflections.getSubTypesOf(interfaceClass);
        if (implementations.isEmpty()) {
            throw new Exception("No implementation for " + interfaceClass.getSimpleName() + " found");
        }

        Class<? extends T> implementationClass = implementations.iterator().next();
        Constructor<? extends T> constructor = implementationClass.getDeclaredConstructor();
        constructor.setAccessible(true);
        return constructor.newInstance();
    }

    private <T> T createInstanceWithArgs(Class<T> interfaceClass, Reflections reflections, List<Object> dependencies) throws Exception {
        Set<Class<? extends T>> implementations = reflections.getSubTypesOf(interfaceClass);
        if (implementations.isEmpty()) {
            throw new Exception("No implementation for " + interfaceClass.getSimpleName() + " found");
        }
        Class<? extends T> implementationClass = implementations.iterator().next();
        Constructor<?>[] constructors = implementationClass.getConstructors();
        Constructor<?> constructor = Arrays.stream(constructors)
                .filter(constructor1 -> constructor1.getParameterCount() == dependencies.size())
                .findFirst().orElseThrow(() -> new Exception("No constructor with " + dependencies.size() + " arguments found"));
        constructor.setAccessible(true);
        Object[] args = new Object[constructor.getParameterCount()];
        for (int i = 0; i < constructor.getParameterCount(); i++) {
            for (Object dependency : dependencies) {
                if (constructor.getParameterTypes()[i].isInstance(dependency)) {
                    args[i] = dependency;
                    break;
                }
            }
        }
        return (T) constructor.newInstance(args);
    }

    private void initializeRepositories() throws Exception {
        Reflections repositoryReflections = new Reflections(UserRepository.class.getPackageName(), new SubTypesScanner(false));
        this.dailyRevenueRepository = createInstance(DailyRevenueRepository.class, repositoryReflections);
        this.userRepository = createInstance(UserRepository.class, repositoryReflections);
    }


    private void initializeRevenueService() throws Exception {
        Reflections serviceReflections = new Reflections(RevenueService.class.getPackageName(), new SubTypesScanner(false));
        this.revenueService = createInstanceWithArgs(RevenueService.class, serviceReflections, Arrays.asList(this.dailyRevenueRepository, this.userRepository));
    }

    private void initializeRevenueController() {
        this.revenueController = new RevenueController(this.revenueService);
    }

    @Test
    public void testCalculateRevenue_CurrentFY_Success() {
        User user = insertUser(UserType.BILLING);
        insertDummyData();
        CalculateRevenueRequestDto requestDto = new CalculateRevenueRequestDto();
        requestDto.setUserId(user.getId());
        requestDto.setRevenueQueryType("CURRENT_FY");
        CalculateRevenueResponseDto calculateRevenueResponseDto = revenueController.calculateRevenue(requestDto);
        assertEquals(ResponseStatus.SUCCESS, calculateRevenueResponseDto.getResponseStatus(), "Response status should be success");
        assertNotNull(calculateRevenueResponseDto.getAggregatedRevenue(), "Aggregated revenue should not be null");
        Date date = new Date();
        int currentMonth = date.getMonth() + 1;
        double expectedRevenueFromFoodSales = currentMonth * (currentMonth+1) * 1000 / 2;
        assertEquals(expectedRevenueFromFoodSales, calculateRevenueResponseDto.getAggregatedRevenue().getRevenueFromFoodSales(), 0.1,"Revenue from food sales should be 7000");
        assertEquals(expectedRevenueFromFoodSales * 0.05, calculateRevenueResponseDto.getAggregatedRevenue().getTotalGst(), 0.1,"Total GST should be 5% of revenue from food sales");
        assertEquals(expectedRevenueFromFoodSales * 0.1, calculateRevenueResponseDto.getAggregatedRevenue().getTotalServiceCharge(), 0.1,"Total service charge should be 10% of revenue from food sales");
    }

    @Test
    public void testCalculateRevenue_PreviousFY_Success() {
        User user = insertUser(UserType.BILLING);
        insertDummyData();
        CalculateRevenueRequestDto requestDto = new CalculateRevenueRequestDto();
        requestDto.setUserId(user.getId());
        requestDto.setRevenueQueryType("PREVIOUS_FY");
        CalculateRevenueResponseDto calculateRevenueResponseDto = revenueController.calculateRevenue(requestDto);
        assertEquals(ResponseStatus.SUCCESS, calculateRevenueResponseDto.getResponseStatus(), "Response status should be success");
        assertNotNull(calculateRevenueResponseDto.getAggregatedRevenue(), "Aggregated revenue should not be null");
        int currentMonth = 12;
        double expectedRevenueFromFoodSales = currentMonth * (currentMonth+1) * 1000 / 2;
        assertEquals(expectedRevenueFromFoodSales, calculateRevenueResponseDto.getAggregatedRevenue().getRevenueFromFoodSales(), 0.1,"Revenue from food sales should be 7000");
        assertEquals(expectedRevenueFromFoodSales * 0.05, calculateRevenueResponseDto.getAggregatedRevenue().getTotalGst(), 0.1,"Total GST should be 5% of revenue from food sales");
        assertEquals(expectedRevenueFromFoodSales * 0.1, calculateRevenueResponseDto.getAggregatedRevenue().getTotalServiceCharge(), 0.1,"Total service charge should be 10% of revenue from food sales");
    }

    @Test
    public void testCalculateRevenue_CurrentMonth_Success() {
        User user = insertUser(UserType.BILLING);
        insertDummyData();
        CalculateRevenueRequestDto requestDto = new CalculateRevenueRequestDto();
        requestDto.setUserId(user.getId());
        requestDto.setRevenueQueryType("CURRENT_MONTH");
        CalculateRevenueResponseDto calculateRevenueResponseDto = revenueController.calculateRevenue(requestDto);
        assertEquals(ResponseStatus.SUCCESS, calculateRevenueResponseDto.getResponseStatus(), "Response status should be success");
        assertNotNull(calculateRevenueResponseDto.getAggregatedRevenue(), "Aggregated revenue should not be null");
        Date date = new Date();
        int currentMonth = date.getMonth() + 1;
        double expectedRevenueFromFoodSales = currentMonth * 1000;
        assertEquals(expectedRevenueFromFoodSales, calculateRevenueResponseDto.getAggregatedRevenue().getRevenueFromFoodSales(), 0.1,"Revenue from food sales should be 7000");
        assertEquals(expectedRevenueFromFoodSales * 0.05, calculateRevenueResponseDto.getAggregatedRevenue().getTotalGst(), 0.1,"Total GST should be 5% of revenue from food sales");
        assertEquals(expectedRevenueFromFoodSales * 0.1, calculateRevenueResponseDto.getAggregatedRevenue().getTotalServiceCharge(), 0.1,"Total service charge should be 10% of revenue from food sales");
    }

    @Test
    public void testCalculateRevenue_PreviousMonth_Success() {
        User user = insertUser(UserType.BILLING);
        insertDummyData();
        CalculateRevenueRequestDto requestDto = new CalculateRevenueRequestDto();
        requestDto.setUserId(user.getId());
        requestDto.setRevenueQueryType("PREVIOUS_MONTH");
        CalculateRevenueResponseDto calculateRevenueResponseDto = revenueController.calculateRevenue(requestDto);
        assertEquals(ResponseStatus.SUCCESS, calculateRevenueResponseDto.getResponseStatus(), "Response status should be success");
        assertNotNull(calculateRevenueResponseDto.getAggregatedRevenue(), "Aggregated revenue should not be null");
        Date date = new Date();
        int prevMonth = date.getMonth() == 0 ? 12 : date.getMonth();
        double expectedRevenueFromFoodSales = prevMonth * 1000;
        assertEquals(expectedRevenueFromFoodSales, calculateRevenueResponseDto.getAggregatedRevenue().getRevenueFromFoodSales(), 0.1,"Revenue from food sales should be 7000");
        assertEquals(expectedRevenueFromFoodSales * 0.05, calculateRevenueResponseDto.getAggregatedRevenue().getTotalGst(), 0.1,"Total GST should be 5% of revenue from food sales");
        assertEquals(expectedRevenueFromFoodSales * 0.1, calculateRevenueResponseDto.getAggregatedRevenue().getTotalServiceCharge(), 0.1,"Total service charge should be 10% of revenue from food sales");
    }

    private User insertUser(UserType userType) {
        User user = new User();
        user.setUserType(userType);
        user.setName("Test User");
        user.setPhone("1234567890");
        return userRepository.save(user);
    }

    private static Calendar getCalendarInstance() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE,0);
        cal.set(Calendar.SECOND,0);
        return cal;
    }

    private void insertDummyData(){
        Calendar calendar = getCalendarInstance();
        int year = calendar.get(Calendar.YEAR);
        int prevYear = year - 1;
        calendar.set(Calendar.YEAR, prevYear);

        for(int i=0; i<12; i++){
            DailyRevenue dailyRevenue = new DailyRevenue();
            double revenueFromFoodSales = 1000 * (i+1);
            dailyRevenue.setRevenueFromFoodSales(revenueFromFoodSales);
            dailyRevenue.setTotalGst(revenueFromFoodSales * 0.05);
            dailyRevenue.setTotalServiceCharge(revenueFromFoodSales * 0.1);
            calendar.set(Calendar.MONTH, i);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dailyRevenue.setDate(calendar.getTime());
            dailyRevenueRepository.save(dailyRevenue);
        }
        calendar.set(Calendar.YEAR, year);
        Date date = new Date();
        for(int i=0; i< date.getMonth() + 1; i++){
            DailyRevenue dailyRevenue = new DailyRevenue();
            double revenueFromFoodSales = 1000 * (i+1);
            dailyRevenue.setRevenueFromFoodSales(revenueFromFoodSales);
            dailyRevenue.setTotalGst(revenueFromFoodSales * 0.05);
            dailyRevenue.setTotalServiceCharge(revenueFromFoodSales * 0.1);
            calendar.set(Calendar.MONTH, i);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            dailyRevenue.setDate(calendar.getTime());
            dailyRevenueRepository.save(dailyRevenue);
        }
    }

}
