package com.example.straffic.mobility.service;

import com.example.straffic.mobility.dto.RouteRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;

@Service
public class RouteService {

    @Value("${api.odsay.key}")
    private String odsayApiKey;

    @Value("${api.odsay.url}")
    private String odsayApiUrl;

    @Value("${api.tmap.key}")
    private String tmapApiKey;

    @Value("${api.seoul.key}")
    private String seoulApiKey;

    @Value("${api.seoul.url}")
    private String seoulApiUrl;

    private final WebClient webClient;

    public RouteService() {
        this.webClient = WebClient.builder()
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    /**
     * 서울시 실시간 지하철 도착 정보 조회
     */
    public Mono<Map<String, Object>> getSeoulSubwayRealtime(String stationName) {
        System.out.println("🚇 실시간 지하철 도착 정보: " + stationName);
        
        // 서울시 API는 역명 끝에 '역'을 제외해야 하는 경우가 많음
        String cleanName = stationName.endsWith("역") ? stationName.substring(0, stationName.length() - 1) : stationName;
        
        String url = String.format("%s/%s/json/realtimeStationArrival/0/10/%s",
                seoulApiUrl, seoulApiKey, cleanName);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    /**
     * 버스 정류장 실시간 도착 정보 조회 (ODsay)
     */
    public Mono<Map<String, Object>> getBusStationRealtime(String stationID) {
        System.out.println("🚌 버스 정류장 도착 정보: " + stationID);
        
        String url = String.format("%s/realtimeStation?stationID=%s&apiKey=%s",
                odsayApiUrl, stationID, odsayApiKey);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    /**
     * 통합 대중교통 경로 검색 (지하철, 버스, KTX 모두 포함)
     */
    public Mono<Map<String, Object>> searchRoute(RouteRequest request) {
        System.out.println("🔍 경로 검색 시작: " + request);

        // ODsay API 경로 검색 엔드포인트
        String url = String.format("%s/searchPubTransPathT?SX=%s&SY=%s&EX=%s&EY=%s&apiKey=%s",
                odsayApiUrl,
                request.getStartX(),
                request.getStartY(),
                request.getEndX(),
                request.getEndY(),
                odsayApiKey
        );

        System.out.println("📡 API URL: " + url);

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .doOnSuccess(response -> {
                    System.out.println("✅ API 응답 성공");
                    // System.out.println("Response: " + response);
                })
                .doOnError(error -> {
                    System.err.println("❌ API 오류: " + error.getMessage());
                })
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    errorResponse.put("status", "failed");
                    return Mono.just(errorResponse);
                });
    }

    /**
     * 버스 전용 검색
     */
    public Mono<Map<String, Object>> searchBus(RouteRequest request) {
        System.out.println("🚌 버스 경로 검색: " + request);

        String url = String.format("%s/searchPubTransPathT?SX=%s&SY=%s&EX=%s&EY=%s&SearchType=2&apiKey=%s",
                odsayApiUrl,
                request.getStartX(),
                request.getStartY(),
                request.getEndX(),
                request.getEndY(),
                odsayApiKey
        );

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    /**
     * 지하철 전용 검색
     */
    public Mono<Map<String, Object>> searchSubway(RouteRequest request) {
        System.out.println("🚇 지하철 경로 검색: " + request);

        String url = String.format("%s/searchPubTransPathT?SX=%s&SY=%s&EX=%s&EY=%s&SearchType=1&apiKey=%s",
                odsayApiUrl,
                request.getStartX(),
                request.getStartY(),
                request.getEndX(),
                request.getEndY(),
                odsayApiKey
        );

        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(errorResponse);
                });
    }

    /**
     * 장소 검색 (Kakao API 활용 가능)
     */
    public Mono<Map<String, Object>> searchPlace(String keyword) {
        System.out.println("📍 장소 검색: " + keyword);
        
        // 여기서는 간단한 응답 반환
        Map<String, Object> response = new HashMap<>();
        response.put("keyword", keyword);
        response.put("status", "success");
        
        return Mono.just(response);
    }

    /**
     * 버스 노선 상세 정보 조회
     */
    public Mono<Map<String, Object>> getBusLaneDetail(String busID) {
        System.out.println("🚌 버스 상세 정보 조회: " + busID);
        String url = String.format("%s/busLaneDetail?busID=%s&apiKey=%s",
                odsayApiUrl, busID, odsayApiKey);
        
        return webClient.get()
                .uri(url)
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .onErrorResume(error -> {
                    Map<String, Object> errorResponse = new HashMap<>();
                    errorResponse.put("error", error.getMessage());
                    return Mono.just(errorResponse);
                });
    }
}
