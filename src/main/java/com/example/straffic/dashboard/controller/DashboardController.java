package com.example.straffic.dashboard.controller;

import com.example.straffic.dashboard.dto.DailyViewsDTO;
import com.example.straffic.dashboard.dto.KtxReservationSummaryDTO;
import com.example.straffic.dashboard.dto.WeeklyStatsDTO;
import com.example.straffic.dashboard.dto.YearlyStatsDTO;
import com.example.straffic.dashboard.service.PageViewStatsService;
import com.example.straffic.dashboard.repository.PageViewHistoryRepository;
import com.example.straffic.mobility.entity.KtxReservationEntity;
import com.example.straffic.mobility.repository.KtxReservationRepository;
import com.example.straffic.board.repository.BoardRepository;
import com.example.straffic.board.repository.CommentRepository;
import com.example.straffic.parking.repository.ParkingRecordRepository;
import com.example.straffic.notice.entity.NoticeEntity;
import com.example.straffic.notice.repository.NoticeRepository;
import com.example.straffic.member.entity.MemberEntity;
import com.example.straffic.member.repository.MemberRepository;
import com.example.straffic.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final KtxReservationRepository ktxReservationRepository;
    private final BoardRepository boardRepository;
    private final CommentRepository commentRepository;
    private final NoticeRepository noticeRepository;
    private final ParkingRecordRepository parkingRecordRepository;
    private final MemberService memberService;
    private final MemberRepository memberRepository;
    private final PageViewStatsService pageViewStatsService;
    private final PageViewHistoryRepository pageViewHistoryRepository;

    @GetMapping("/admin/dashboard")
    public String dashboard(@RequestParam(value = "ktxPage", defaultValue = "1") int ktxPage,
                            Model model) {
        // DailyViewsDTO dailyViews = pageViewStatsService.getTotals(); // 기존: 누적 조회수 반환
        DailyViewsDTO dailyViews = getTodayDailyViews(); // 변경: 오늘 하루 조회수 반환

        WeeklyStatsDTO weeklyStats = buildWeeklyStats("ALL");
        YearlyStatsDTO yearlyStats = buildYearlyStats("ALL");

        int ktxPageIndex = Math.max(ktxPage - 1, 0);
        Page<KtxReservationEntity> ktxPageResult = loadKtxReservationsPage(ktxPageIndex);
        List<KtxReservationSummaryDTO> ktxReservations = ktxPageResult.getContent()
                .stream()
                .map(this::toSummaryDto)
                .collect(Collectors.toList());
        int ktxTotalPages = Math.max(ktxPageResult.getTotalPages(), 1);
        int ktxCurrentPage = Math.min(ktxPageIndex + 1, ktxTotalPages);

        long totalBoards = boardRepository.count();
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.LocalDateTime start = today.atStartOfDay();
        java.time.LocalDateTime end = today.plusDays(1).atStartOfDay();
        long todayBoards = boardRepository.countByCreatedAtBetween(start, end);
        long answeredBoards = commentRepository.countBoardsWithAdminComments("ADMIN");
        long unansweredBoards = Math.max(totalBoards - answeredBoards, 0);
        List<NoticeEntity> recent3Notices = noticeRepository.findTop3ByOrderByCreatedAtDesc();

        model.addAttribute("dailyViews", dailyViews);
        model.addAttribute("weeklyStats", weeklyStats);
        model.addAttribute("yearlyStats", yearlyStats);
        model.addAttribute("ktxReservations", ktxReservations);
        model.addAttribute("ktxPage", ktxCurrentPage);
        model.addAttribute("ktxTotalPages", ktxTotalPages);
        model.addAttribute("boardTodayCount", todayBoards);
        model.addAttribute("boardTotalCount", totalBoards);
        model.addAttribute("boardAnsweredCount", answeredBoards);
        model.addAttribute("boardUnansweredCount", unansweredBoards);
        model.addAttribute("recent3Notices", recent3Notices);

        return "dashboard/dashboard";
    }

    private DailyViewsDTO getTodayDailyViews() {
        LocalDate today = LocalDate.now();
        LocalDateTime start = today.atStartOfDay();
        LocalDateTime end = today.plusDays(1).atStartOfDay();

        int parking = (int) pageViewHistoryRepository.countByPageNameAndViewedAtBetween("PARKING", start, end);
        int ktx = (int) pageViewHistoryRepository.countByPageNameAndViewedAtBetween("KTX", start, end);
        int bike = (int) pageViewHistoryRepository.countByPageNameAndViewedAtBetween("WAYFINDING", start, end);
        int subway = (int) pageViewHistoryRepository.countByPageNameAndViewedAtBetween("SUBWAY", start, end);

        return new DailyViewsDTO(parking, ktx, bike, subway);
    }

    @GetMapping("/dashboard/security")
    public String security(@RequestParam(value = "page", defaultValue = "1") int page,
                           Model model) {
        int pageIndex = Math.max(page - 1, 0);
        Page<MemberEntity> memberPage = memberService.entitypage(pageIndex);
        int totalPage = memberPage.getTotalPages();
        if (totalPage == 0) {
            totalPage = 1;
        }
        int nowpage = memberPage.getNumber() + 1;

        LocalDate today = LocalDate.now();
        LocalDateTime startOfToday = today.atStartOfDay();

        long totalBasic = memberRepository.countByProviderIsNull();
        long totalGoogle = memberRepository.countByProvider("google");
        long totalNaver = memberRepository.countByProvider("naver");
        long totalKakao = memberRepository.countByProvider("kakao");

        long todayBasic = memberRepository.countByProviderIsNullAndCreatedAtAfter(startOfToday);
        long todayGoogle = memberRepository.countByProviderAndCreatedAtAfter("google", startOfToday);
        long todayNaver = memberRepository.countByProviderAndCreatedAtAfter("naver", startOfToday);
        long todayKakao = memberRepository.countByProviderAndCreatedAtAfter("kakao", startOfToday);

        List<Long> totalCounts = List.of(totalBasic, totalGoogle, totalNaver, totalKakao);
        List<Long> todayCounts = List.of(todayBasic, todayGoogle, todayNaver, todayKakao);

        model.addAttribute("list", memberPage.getContent());
        model.addAttribute("totalPage", totalPage);
        model.addAttribute("nowpage", nowpage);
        model.addAttribute("totalCounts", totalCounts);
        model.addAttribute("todayCounts", todayCounts);

        return "security/security";
    }

    @GetMapping("/admin/dashboard/api/stats")
    @org.springframework.web.bind.annotation.ResponseBody
    public java.util.Map<String, Object> getStats(@RequestParam String type, @RequestParam String platform) {
        java.util.Map<String, Object> result = new java.util.HashMap<>();
        if ("weekly".equals(type)) {
            result.put("stats", buildWeeklyStats(platform));
        } else if ("yearly".equals(type)) {
            result.put("stats", buildYearlyStats(platform));
        }
        return result;
    }

    private WeeklyStatsDTO buildWeeklyStats(String platform) {
        LocalDate today = LocalDate.now();
        int dayOfWeekValue = today.getDayOfWeek().getValue();
        LocalDate monday = today.minusDays((dayOfWeekValue + 6) % 7);
        LocalDate mondayPrevWeek = monday.minusWeeks(1);

        List<String> labels = Arrays.asList("월", "화", "수", "목", "금", "토", "일");
        List<Integer> current = new ArrayList<>();
        List<Integer> previous = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            LocalDate day = monday.plusDays(i);
            LocalDate prevDay = mondayPrevWeek.plusDays(i);
            current.add(countTotalActivityForDate(day, platform));
            previous.add(countTotalActivityForDate(prevDay, platform));
        }

        return new WeeklyStatsDTO(labels, current, previous);
    }

    private YearlyStatsDTO buildYearlyStats(String platform) {
        LocalDate today = LocalDate.now();
        int currentYear = today.getYear();
        int previousYear = currentYear - 1;

        List<String> labels = Arrays.asList("1월", "2월", "3월", "4월", "5월", "6월",
                "7월", "8월", "9월", "10월", "11월", "12월");
        List<Integer> current = new ArrayList<>();
        List<Integer> previous = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            current.add(countTotalActivityForMonth(currentYear, month, platform));
            previous.add(countTotalActivityForMonth(previousYear, month, platform));
        }

        return new YearlyStatsDTO(labels, current, previous);
    }

    private int countTotalActivityForDate(LocalDate date, String platform) {
        LocalDateTime start = date.atStartOfDay();
        LocalDateTime end = date.plusDays(1).atStartOfDay();
        long total = 0;

        if ("ALL".equals(platform)) {
            long parking = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("PARKING", start, end);
            long ktx = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("KTX", start, end);
            long bike = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("WAYFINDING", start, end);
            total = parking + ktx + bike;
        } else if ("PARKING".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("PARKING", start, end);
        } else if ("KTX".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("KTX", start, end);
        } else if ("WAYFINDING".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("WAYFINDING", start, end);
        } else if ("SUBWAY".equals(platform)) {
             total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("SUBWAY", start, end);
        }

        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    private int countTotalActivityForMonth(int year, int month, String platform) {
        LocalDate startDate = LocalDate.of(year, month, 1);
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = startDate.plusMonths(1).atStartOfDay();
        long total = 0;

        if ("ALL".equals(platform)) {
            long parking = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("PARKING", start, end);
            long ktx = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("KTX", start, end);
            long bike = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("WAYFINDING", start, end);
            total = parking + ktx + bike;
        } else if ("PARKING".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("PARKING", start, end);
        } else if ("KTX".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("KTX", start, end);
        } else if ("WAYFINDING".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("WAYFINDING", start, end);
        } else if ("SUBWAY".equals(platform)) {
            total = pageViewHistoryRepository.countByPageNameAndViewedAtBetween("SUBWAY", start, end);
        }

        if (total > Integer.MAX_VALUE) {
            return Integer.MAX_VALUE;
        }
        return (int) total;
    }

    private Page<KtxReservationEntity> loadKtxReservationsPage(int page) {
        if (page < 0) {
            page = 0;
        }
        PageRequest pageable = PageRequest.of(page, 5, Sort.by(Sort.Direction.DESC, "reservedAt"));
        return ktxReservationRepository.findAll(pageable);
    }

    private KtxReservationSummaryDTO toSummaryDto(KtxReservationEntity entity) {
        String paidAt = entity.getReservedAt() != null
                ? entity.getReservedAt().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
                : null;

        String amount = entity.getTotalPrice() != null
                ? entity.getTotalPrice().toString()
                : null;

        String memberId = entity.getMemberId();
        if (memberId != null) {
            // 1. Social ID filtering
            boolean isSocial = memberId.startsWith("naver_") || memberId.startsWith("kakao_") || memberId.startsWith("google_");
            if (isSocial) {
                int idx = memberId.indexOf("_");
                if (idx != -1 && memberId.length() > idx + 4) {
                    memberId = memberId.substring(0, idx + 4);
                }
            }

            // 2. Length check: Truncate if longer than 6 chars
            if (memberId.length() > 6) {
                memberId = memberId.substring(0, 6) + "...";
            }
        }

        return new KtxReservationSummaryDTO(
                memberId,
                entity.getPassengerCount(),
                entity.getTrainNo(),
                entity.getDeparture(),
                entity.getArrival(),
                paidAt,
                amount,
                entity.getSeats()
        );
    }

    @GetMapping("/parking")
    public String parkin1() {
        return "redirect:/parking/output";
    }

    @GetMapping("/mobility/ktx")
    public String mobility1() {
        return "redirect:/ktx";
    }

    @GetMapping("/mobility/bike")
    public String mobility2() {
        return "redirect:/wayfinding";
    }

    @GetMapping("/dashboard/security/again")
    public String security1(@RequestParam(value = "page", defaultValue = "1") int page,
                            Model model) {
        return security(page, model);
    }

}
