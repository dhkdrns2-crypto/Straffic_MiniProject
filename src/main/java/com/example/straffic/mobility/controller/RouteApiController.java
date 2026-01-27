package com.example.straffic.mobility.controller;

import com.example.straffic.mobility.dto.RouteRequest;
import com.example.straffic.mobility.service.RouteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/api")
public class RouteApiController {

    @Autowired
    private RouteService routeService;

    @PostMapping("/route/search")
    public Mono<ResponseEntity<Map<String, Object>>> searchRoute(@RequestBody RouteRequest request) {
        return routeService.searchRoute(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/route/subway")
    public Mono<ResponseEntity<Map<String, Object>>> searchSubway(@RequestBody RouteRequest request) {
        return routeService.searchSubway(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/route/bus")
    public Mono<ResponseEntity<Map<String, Object>>> searchBus(@RequestBody RouteRequest request) {
        return routeService.searchBus(request)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/bus/detail")
    public Mono<ResponseEntity<Map<String, Object>>> getBusLaneDetail(@RequestParam String busID) {
        return routeService.getBusLaneDetail(busID)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/subway/realtime")
    public Mono<ResponseEntity<Map<String, Object>>> getSubwayRealtime(@RequestParam String stationName) {
        return routeService.getSeoulSubwayRealtime(stationName)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/bus/station/realtime")
    public Mono<ResponseEntity<Map<String, Object>>> getBusStationRealtime(@RequestParam String stationID) {
        return routeService.getBusStationRealtime(stationID)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "OK",
                "service", "M-MaaS",
                "version", "1.0.0"
        ));
    }
}
