package com.example.straffic.mobility.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.*;

@Controller
public class KtxController {
    @Value("${kakao.js.key:}")
    private String kakaoJsKey;

    @GetMapping("/ktx")
    public String ktxMain(Model model) {
        model.addAttribute("pageTitle", "KTX 예매");
        model.addAttribute("kakaoKey", kakaoJsKey);
        model.addAttribute("stations", getStations());
        model.addAttribute("trains", generateTrains("서울", "부산"));
        return "mobility/ktx";
    }

    @GetMapping("/ktx/search")
    @ResponseBody
    public Map<String, Object> searchTrains(@RequestParam String departure,
                                            @RequestParam String arrival,
                                            @RequestParam String date,
                                            @RequestParam(defaultValue = "1") int passengers) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("departure", departure);
        result.put("arrival", arrival);
        result.put("date", date);
        result.put("passengers", passengers);
        result.put("trains", generateTrains(departure, arrival));
        return result;
    }

    @GetMapping("/ktx/api/reserve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reserve(@RequestParam String trainNo,
                                                       @RequestParam String from,
                                                       @RequestParam String to,
                                                       @RequestParam String depTime,
                                                       @RequestParam String arrTime,
                                                       @RequestParam String seats,
                                                       @RequestParam String price) {
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("reservationId", "KTX" + System.currentTimeMillis());
        result.put("trainNo", trainNo);
        result.put("from", from);
        result.put("to", to);
        result.put("depTime", depTime);
        result.put("arrTime", arrTime);
        result.put("seats", seats);
        result.put("price", price);
        result.put("message", "예약이 완료되었습니다.");
        return ResponseEntity.ok(result);
    }

    private List<String> getStations() {
        return Arrays.asList("서울", "용산", "광명", "수원", "천안아산", "대전", "동대구", "부산");
    }

    private List<Map<String, Object>> generateTrains(String from, String to) {
        List<Map<String, Object>> trains = new ArrayList<>();
        String[] times = {"06:00", "07:00", "08:00", "09:00", "10:00", "11:00", "12:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00"};
        int[] durations = {150, 145, 150, 148, 155, 150, 152, 148, 150, 155, 150, 148, 152, 150};
        int[] prices = {59800, 59800, 59800, 59800, 53200, 53200, 53200, 53200, 53200, 53200, 59800, 59800, 59800, 59800};
        for (int i = 0; i < times.length; i++) {
            Map<String, Object> train = new HashMap<>();
            train.put("trainNo", "KTX " + String.format("%03d", (i * 2) + 1));
            train.put("departure", from);
            train.put("arrival", to);
            train.put("departureTime", times[i]);
            train.put("arrivalTime", calculateArrivalTime(times[i], durations[i]));
            train.put("duration", durations[i] + "분");
            train.put("price", String.format("%,d", prices[i]));
            train.put("availableSeats", 10 + (int)(Math.random() * 30));
            trains.add(train);
        }
        return trains;
    }

    private String calculateArrivalTime(String departure, int durationMinutes) {
        String[] parts = departure.split(":");
        int hours = Integer.parseInt(parts[0]);
        int minutes = Integer.parseInt(parts[1]);
        minutes += durationMinutes;
        hours += minutes / 60;
        minutes = minutes % 60;
        hours = hours % 24;
        return String.format("%02d:%02d", hours, minutes);
    }
}
