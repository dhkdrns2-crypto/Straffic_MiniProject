package com.example.straffic.mobility.controller;

import com.example.straffic.mobility.entity.SubwayStationEntity;
import com.example.straffic.mobility.repository.SubwayStationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
@RequiredArgsConstructor
public class SubwayController {
    @Value("${kakao.js.key:}")
    private String kakaoJsKey;

    private final SubwayStationRepository subwayStationRepository;

    @GetMapping("/subway")
    public String subwayMain(Model model) {
        model.addAttribute("pageTitle", "지하철 실시간");
        model.addAttribute("kakaoKey", kakaoJsKey);
        model.addAttribute("lines", getLines());
        model.addAttribute("stations", getDefaultStations());
        return "mobility/subway";
    }

    @GetMapping("/subway/api/stations/by-line")
    @ResponseBody
    public Map<String, Object> getStationsByLine(@RequestParam String lineNumber) {
        Map<String, Object> result = new HashMap<>();
        List<SubwayStationEntity> entities = subwayStationRepository.findByLineNumber(lineNumber);
        List<String> stationNames = new ArrayList<>();
        for (SubwayStationEntity entity : entities) {
            stationNames.add(entity.getStationName());
        }
        if (stationNames.isEmpty() && "1".equals(lineNumber)) {
            stationNames.addAll(getDefaultStations());
        }
        result.put("success", true);
        result.put("lineNumber", lineNumber);
        result.put("stations", stationNames);
        return result;
    }

    @GetMapping("/subway/api/stations/stats")
    @ResponseBody
    public Map<String, Object> getStationStats() {
        Map<String, Object> result = new HashMap<>();
        List<String> mainLines = Arrays.asList("1", "2", "3", "4", "5", "6", "7", "8", "9");
        long total = subwayStationRepository.countByLineNumberIn(mainLines);
        result.put("success", true);
        result.put("totalStations", total);
        return result;
    }

    @GetMapping("/subway/api/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchStation(@RequestParam String keyword) {
        List<SubwayStationEntity> entities = subwayStationRepository.findTop20ByStationNameContaining(keyword);
        List<Map<String, Object>> items = new ArrayList<>();
        if (!entities.isEmpty()) {
            for (SubwayStationEntity station : entities) {
                Map<String, Object> m = new HashMap<>();
                String line = station.getLineNumber();
                String lineName;
                if (line != null && line.matches("\\d+")) {
                    lineName = line + "호선";
                } else {
                    lineName = line;
                }
                m.put("stationName", station.getStationName());
                m.put("lineNumber", line);
                m.put("lineName", lineName);
                m.put("latitude", station.getLatitude());
                m.put("longitude", station.getLongitude());
                items.add(m);
            }
            return ResponseEntity.ok(items);
        }

        for (String name : getAllStations()) {
            if (name.contains(keyword)) {
                Map<String, Object> m = new HashMap<>();
                m.put("stationName", name + "역");
                m.put("lineNumber", getDefaultStations().contains(name) ? 1 : 2);
                m.put("lineName", getDefaultStations().contains(name) ? "1호선" : "2호선");
                m.put("latitude", 37.55);
                m.put("longitude", 126.97);
                items.add(m);
            }
        }
        if (items.size() > 10) {
            items = items.subList(0, 10);
        }
        return ResponseEntity.ok(items);
    }

    @GetMapping("/subway/api/arrival/{stationName}")
    @ResponseBody
    public Map<String, Object> getArrivalInfo(@PathVariable String stationName) {
        Map<String, Object> result = new HashMap<>();
        result.put("stationName", stationName);
        result.put("arrivals", generateArrivalInfo("1", stationName));
        result.put("message", "실시간 도착 정보");
        return result;
    }

    @GetMapping("/subway/api/route")
    @ResponseBody
    public Map<String, Object> findRoute(@RequestParam String startStation,
                                         @RequestParam String endStation) {
        Map<String, Object> route = new HashMap<>();
        route.put("success", true);
        route.put("start", startStation);
        route.put("end", endStation);
        route.put("durationMinutes", 27);
        route.put("transfers", 1);
        route.put("stations", Arrays.asList(startStation, "교대", "서초", "방배", endStation));
        return route;
    }

    private List<Map<String, Object>> getLines() {
        List<Map<String, Object>> lines = new ArrayList<>();
        String[][] lineData = {
                {"1", "1호선", "#0052A4"},
                {"2", "2호선", "#00A84D"},
                {"3", "3호선", "#EF7C1C"},
                {"4", "4호선", "#00A5DE"},
                {"5", "5호선", "#996CAC"},
                {"6", "6호선", "#CD7C2F"},
                {"7", "7호선", "#747F00"},
                {"8", "8호선", "#E6186C"},
                {"9", "9호선", "#BDB092"}
        };
        for (String[] line : lineData) {
            Map<String, Object> lineInfo = new HashMap<>();
            lineInfo.put("id", line[0]);
            lineInfo.put("name", line[1]);
            lineInfo.put("color", line[2]);
            lines.add(lineInfo);
        }
        return lines;
    }

    private List<String> getDefaultStations() {
        return Arrays.asList(
                "소요산", "동두천", "의정부", "회룡", "망월사", "도봉산", "도봉", "방학",
                "창동", "녹천", "월계", "광운대", "석계", "신이문", "외대앞", "회기",
                "청량리", "제기동", "신설동", "동묘앞", "동대문", "종로5가", "종로3가", "종각",
                "시청", "서울역", "남영", "용산", "노량진", "대방", "신도림", "구로",
                "가산디지털단지", "독산", "금천구청", "관악", "안양", "명학", "금정", "군포",
                "당정", "의왕", "성균관대", "화서", "수원", "세류", "병점", "세마",
                "오산대", "오산", "진위", "송탄", "서정리", "평택", "성환", "직산",
                "두정", "천안", "봉명", "쌍용", "아산", "배방", "온양온천", "신창",
                "부평", "인천", "동인천", "주안", "간석", "동암", "백운", "부개"
        );
    }

    private List<String> getAllStations() {
        List<String> stations = new ArrayList<>(getDefaultStations());
        stations.addAll(Arrays.asList(
                "강남", "역삼", "선릉", "삼성", "종합운동장", "잠실", "잠실새내",
                "홍대입구", "합정", "당산", "영등포구청", "신도림", "대림", "구로디지털단지",
                "교대", "서초", "방배", "사당", "낙성대", "서울대입구", "신림"
        ));
        return stations;
    }

    private List<Map<String, Object>> generateArrivalInfo(String line, String station) {
        List<Map<String, Object>> arrivals = new ArrayList<>();
        Map<String, Object> up1 = new HashMap<>();
        up1.put("direction", "상행");
        up1.put("destination", "소요산");
        up1.put("trainType", "급행");
        up1.put("carriages", "8량");
        up1.put("congestion", "보통");
        up1.put("arrivalTime", 2);
        up1.put("stationsBefore", 2);
        up1.put("status", "곧 도착");
        arrivals.add(up1);
        Map<String, Object> down1 = new HashMap<>();
        down1.put("direction", "하행");
        down1.put("destination", "인천/신창");
        down1.put("trainType", "완행");
        down1.put("carriages", "10량");
        down1.put("congestion", "여유");
        down1.put("arrivalTime", 5);
        down1.put("stationsBefore", 3);
        down1.put("status", "운행 중");
        arrivals.add(down1);
        return arrivals;
    }
}
