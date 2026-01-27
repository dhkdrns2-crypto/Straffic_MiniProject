package com.example.straffic.parking.controller;

import com.example.straffic.dashboard.service.PageViewStatsService;
import com.example.straffic.parking.entity.ParkingRecordEntity;
import com.example.straffic.parking.repository.ParkingRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;

@Controller
@RequiredArgsConstructor
public class ParkingController {
    private final ParkingRecordRepository parkingRecordRepository;
    private final PageViewStatsService pageViewStatsService;

    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    @GetMapping("/parking/output")
    public String parkingOutput(Model model) {
        pageViewStatsService.increaseView("PARKING");
        model.addAttribute("pageTitle", "주차 관리");
        model.addAttribute("spots", getSpots());
        return "parking/dashboard";
    }

    @PostMapping("/parking/output")
    public String calculateFee(@RequestParam String carNumber,
                               @RequestParam String carType,
                               @RequestParam String parkingSpot,
                               @RequestParam("entryTime") String entryTimeString,
                               @RequestParam("exitTime") String exitTimeString,
                               Model model) {
        LocalDateTime entryTime = LocalDateTime.parse(entryTimeString, DATETIME_FORMATTER);
        LocalDateTime exitTime = LocalDateTime.parse(exitTimeString, DATETIME_FORMATTER);

        if (exitTime.isBefore(entryTime)) {
            model.addAttribute("error", "출차 시간이 입차 시간보다 빠를 수 없습니다.");
            model.addAttribute("spots", getSpots());
            return "parking/output";
        }

        long minutes = Duration.between(entryTime, exitTime).toMinutes();
        if (minutes == 0) {
            minutes = 1;
        }

        int fee = calculateBaseFee(minutes);
        fee = applyDiscount(fee, carType);

        ParkingRecordEntity record = new ParkingRecordEntity();
        record.setParkingSpot(parkingSpot);
        record.setCarNumber(carNumber);
        record.setCarType(carType);
        record.setEntryTime(entryTime);
        record.setExitTime(exitTime);
        record.setDurationMinutes(minutes);
        record.setFee(fee);
        parkingRecordRepository.save(record);

        model.addAttribute("pageTitle", "주차 관리");
        model.addAttribute("spots", getSpots());
        model.addAttribute("carNumber", carNumber);
        model.addAttribute("carType", carType);
        model.addAttribute("parkingSpot", parkingSpot);
        model.addAttribute("entryTime", entryTime);
        model.addAttribute("exitTime", exitTime);
        model.addAttribute("durationMinutes", minutes);
        model.addAttribute("fee", fee);
        return "parking/output";
    }

    private int calculateBaseFee(long minutes) {
        int fee = 1000;
        if (minutes <= 30) {
            return fee;
        }
        long extra = minutes - 30;
        long blocks = (extra + 9) / 10;
        fee += blocks * 500;
        return fee;
    }

    private int applyDiscount(int fee, String carType) {
        if ("경차".equals(carType)) {
            return fee / 2;
        }
        if ("전기차".equals(carType)) {
            return (int) Math.round(fee * 0.7);
        }
        if ("장애인".equals(carType)) {
            return 0;
        }
        return fee;
    }

    private List<String> getSpots() {
        return Arrays.asList("A-1", "A-2", "A-3", "A-4", "A-5", "A-6", "A-7", "A-8", "A-9", "A-10");
    }
}

