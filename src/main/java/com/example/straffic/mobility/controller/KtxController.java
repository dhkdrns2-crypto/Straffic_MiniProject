package com.example.straffic.mobility.controller;

import com.example.straffic.mobility.entity.KtxReservationEntity;
import com.example.straffic.mobility.entity.KtxTrainEntity;
import com.example.straffic.mobility.repository.KtxReservationRepository;
import com.example.straffic.mobility.repository.KtxTrainRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
@RequiredArgsConstructor
public class KtxController {

    private final KtxReservationRepository reservationRepository;
    private final KtxTrainRepository trainRepository;

    @PostConstruct
    public void seedTrains() {
        if (trainRepository.count() > 0) return;

        List<KtxTrainEntity> trains = new ArrayList<>();
        // Dates: Feb 27 - Feb 28, 2026 (Handling user's "Feb 30" intent with valid dates)
        LocalDate[] dates = {
            LocalDate.of(2026, 2, 27),
            LocalDate.of(2026, 2, 28)
        };

        String[] stations = {"서울", "부산", "동대구", "대전", "광명", "천안아산", "용산", "수원"};
        
        // Generate round trips between all station pairs
        for (LocalDate date : dates) {
            int trainNum = 101;
            
            // Major routes
            createRoute(trains, date, "서울", "부산", trainNum++, "05:00", "07:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "06:00", "08:40", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "07:00", "09:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "08:00", "10:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "09:00", "11:40", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "10:00", "12:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "12:00", "14:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "15:00", "17:30", 59800);
            createRoute(trains, date, "서울", "부산", trainNum++, "18:00", "20:30", 59800);
            
            createRoute(trains, date, "부산", "서울", trainNum++, "05:30", "08:00", 59800);
            createRoute(trains, date, "부산", "서울", trainNum++, "07:30", "10:00", 59800);
            createRoute(trains, date, "부산", "서울", trainNum++, "09:30", "12:00", 59800);
            createRoute(trains, date, "부산", "서울", trainNum++, "13:30", "16:00", 59800);
            createRoute(trains, date, "부산", "서울", trainNum++, "16:30", "19:00", 59800);
            createRoute(trains, date, "부산", "서울", trainNum++, "19:30", "22:00", 59800);

            // Other routes (Simplified for example)
            createRoute(trains, date, "용산", "광주송정", trainNum++, "06:20", "08:10", 46800);
            createRoute(trains, date, "용산", "광주송정", trainNum++, "09:20", "11:10", 46800);
            createRoute(trains, date, "광주송정", "용산", trainNum++, "13:20", "15:10", 46800);
            
            createRoute(trains, date, "서울", "대전", trainNum++, "07:00", "08:00", 23700);
            createRoute(trains, date, "대전", "서울", trainNum++, "09:00", "10:00", 23700);
            
            createRoute(trains, date, "서울", "동대구", trainNum++, "08:00", "09:50", 43500);
            createRoute(trains, date, "동대구", "서울", trainNum++, "11:00", "12:50", 43500);
        }
        trainRepository.saveAll(trains);
    }

    private void createRoute(List<KtxTrainEntity> list, LocalDate date, String dep, String arr, int num, String dTime, String aTime, int price) {
        KtxTrainEntity t = new KtxTrainEntity();
        t.setTrainNo("KTX" + num);
        t.setDeparture(dep);
        t.setArrival(arr);
        t.setDepartureTime(dTime);
        t.setArrivalTime(aTime);
        t.setDuration(calculateDuration(dTime, aTime));
        t.setPrice(price);
        t.setTotalSeats(20);
        t.setTravelDate(date);
        list.add(t);
    }
    
    private String calculateDuration(String start, String end) {
        LocalTime s = LocalTime.parse(start);
        LocalTime e = LocalTime.parse(end);
        java.time.Duration d = java.time.Duration.between(s, e);
        long hours = d.toHours();
        long minutes = d.toMinutesPart();
        return hours + "시간 " + minutes + "분";
    }

    @GetMapping("/ktx")
    public String ktxMain(Model model) {
        model.addAttribute("pageTitle", "KTX 예매");
        return "mobility/ktx";
    }

    @GetMapping("/ktx/search")
    @ResponseBody
    public Map<String, Object> searchTrains(@RequestParam String departure,
                                            @RequestParam String arrival,
                                            @RequestParam String date,
                                            @RequestParam(defaultValue = "1") int passengers) {
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> trains = new ArrayList<>();

        LocalDate travelDate = parseDateOrToday(date);
        
        List<KtxTrainEntity> entities = trainRepository.findByDepartureAndArrivalAndTravelDate(departure, arrival, travelDate);

        for (KtxTrainEntity entity : entities) {
            Set<Integer> reservedSeats = getReservedSeats(entity.getTrainNo(), travelDate);
            int availableSeats = entity.getTotalSeats() - reservedSeats.size();

            if (availableSeats < passengers) {
                continue;
            }

            Map<String, Object> t = new HashMap<>();
            t.put("trainNo", entity.getTrainNo());
            t.put("departure", entity.getDeparture());
            t.put("arrival", entity.getArrival());
            t.put("departureTime", entity.getDepartureTime());
            t.put("arrivalTime", entity.getArrivalTime());
            t.put("duration", entity.getDuration());
            t.put("price", entity.getPrice());
            t.put("availableSeats", availableSeats);
            trains.add(t);
        }

        result.put("success", true);
        result.put("departure", departure);
        result.put("arrival", arrival);
        result.put("date", travelDate.toString());
        result.put("passengers", passengers);
        result.put("trains", trains);
        return result;
    }

    @GetMapping("/ktx/api/seats")
    @ResponseBody
    public Map<String, Object> getSeatStatus(@RequestParam String trainNo,
                                             @RequestParam String date) {
        LocalDate travelDate = parseDateOrToday(date);
        Set<Integer> reservedSeats = getReservedSeats(trainNo, travelDate);

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("trainNo", trainNo);
        result.put("date", travelDate.toString());
        result.put("occupiedSeats", reservedSeats);
        result.put("totalSeats", 20);
        result.put("maxSelectable", 4);
        return result;
    }

    @PostMapping("/ktx/api/reserve")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reserve(@RequestParam String trainNo,
                                                       @RequestParam("from") String fromStation,
                                                       @RequestParam("to") String toStation,
                                                       @RequestParam("depTime") String depTime,
                                                       @RequestParam("arrTime") String arrTime,
                                                       @RequestParam String seats,
                                                       @RequestParam int price,
                                                       @RequestParam String date,
                                                       @RequestParam int passengers,
                                                       Authentication authentication) {
        LocalDate travelDate = parseDateOrToday(date);
        List<Integer> requestedSeats = parseSeatNumbers(seats);

        Map<String, Object> result = new HashMap<>();

        int maxPassengers = Math.min(passengers, 2);
        if (maxPassengers < 1) {
            maxPassengers = 1;
        }

        if (requestedSeats.isEmpty() || requestedSeats.size() > maxPassengers) {
            result.put("success", false);
            result.put("message", "좌석은 인원 수와 같게 선택해야 하며, 최대 2명까지 예약할 수 있습니다.");
            return ResponseEntity.ok(result);
        }

        Set<Integer> reservedSeats = getReservedSeats(trainNo, travelDate);
        List<Integer> conflict = new ArrayList<>();
        for (Integer s : requestedSeats) {
            if (reservedSeats.contains(s)) {
                conflict.add(s);
            }
        }
        if (!conflict.isEmpty()) {
            result.put("success", false);
            result.put("message", "이미 예약된 좌석이 포함되어 있습니다: " + conflict);
            result.put("occupiedSeats", reservedSeats);
            return ResponseEntity.ok(result);
        }

        int totalPrice = price * requestedSeats.size();
        String memberId = authentication != null ? authentication.getName() : null;

        KtxReservationEntity entity = new KtxReservationEntity();
        entity.setMemberId(memberId);
        entity.setPassengerCount(maxPassengers);
        entity.setTrainNo(trainNo);
        entity.setDeparture(fromStation);
        entity.setArrival(toStation);
        entity.setDepartureTime(depTime);
        entity.setArrivalTime(arrTime);
        entity.setTravelDate(travelDate);
        entity.setSeats(joinSeats(requestedSeats));
        entity.setSeatCount(requestedSeats.size());
        entity.setTotalPrice(totalPrice);
        entity.setReservedAt(LocalDateTime.now());
        reservationRepository.save(entity);

        String reservationId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        result.put("success", true);
        result.put("id", entity.getId()); // DB ID for cancellation
        result.put("reservationId", reservationId);
        result.put("trainNo", trainNo);
        result.put("from", fromStation);
        result.put("to", toStation);
        result.put("depTime", depTime);
        result.put("arrTime", arrTime);
        result.put("seats", requestedSeats);
        result.put("price", totalPrice);
        result.put("date", travelDate.toString());

        return ResponseEntity.ok(result);
    }

    @PostMapping("/ktx/api/cancel")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> cancel(@RequestParam Long id) {
        Map<String, Object> result = new HashMap<>();
        try {
            reservationRepository.deleteById(id);
            result.put("success", true);
            result.put("message", "예약이 취소되었습니다.");
        } catch (Exception e) {
            result.put("success", false);
            result.put("message", "예약 취소 중 오류가 발생했습니다: " + e.getMessage());
        }
        return ResponseEntity.ok(result);
    }

    private LocalDate parseDateOrToday(String date) {
        try {
            return LocalDate.parse(date);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private Set<Integer> getReservedSeats(String trainNo, LocalDate date) {
        Set<Integer> reserved = new HashSet<>();
        reservationRepository.findByTrainNoAndTravelDate(trainNo, date)
                .forEach(r -> {
                    List<Integer> list = parseSeatNumbers(r.getSeats());
                    reserved.addAll(list);
                });
        return reserved;
    }

    private List<Integer> parseSeatNumbers(String seats) {
        List<Integer> list = new ArrayList<>();
        if (seats == null) {
            return list;
        }
        String[] parts = seats.split(",");
        for (String p : parts) {
            String trimmed = p.trim();
            if (!trimmed.isEmpty()) {
                try {
                    list.add(Integer.parseInt(trimmed));
                } catch (NumberFormatException ignored) {
                }
            }
        }
        return list;
    }

    private String joinSeats(List<Integer> seats) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < seats.size(); i++) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(seats.get(i));
        }
        return sb.toString();
    }
}
