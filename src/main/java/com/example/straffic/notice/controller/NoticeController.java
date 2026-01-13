package com.example.straffic.notice.controller;

import com.example.straffic.notice.entity.NoticeEntity;
import com.example.straffic.notice.service.NoticeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class NoticeController {

    private final NoticeService noticeService;

    @GetMapping("/notice/list")
    public String list(@RequestParam(value = "page", defaultValue = "0") int page,
                       @RequestParam(value = "q", required = false) String q,
                       @RequestParam(value = "tag", required = false) String tag,
                       Model model) {
        Page<NoticeEntity> notices = noticeService.list(page, q, tag);
        model.addAttribute("notices", notices);
        model.addAttribute("q", q);
        model.addAttribute("tag", tag);
        return "notice/list";
    }

    @GetMapping("/notice/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        NoticeEntity notice = noticeService.findAndIncreaseViews(id);
        model.addAttribute("notice", notice);
        return "notice/detail";
    }

    @GetMapping("/notice/image/{imageId}")
    public ResponseEntity<byte[]> image(@PathVariable Long imageId) {
        NoticeEntity notice = noticeService.get(imageId);
        byte[] data = notice.getImageData();
        
        MediaType mediaType = MediaType.IMAGE_JPEG;
        try {
            if (notice.getImageContentType() != null) {
                mediaType = MediaType.parseMediaType(notice.getImageContentType());
            }
        } catch (Exception e) {
            // fallback to JPEG
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .body(data);
    }

    @GetMapping("/notice/admin/create")
    public String createForm(Model model) {
        model.addAttribute("mode", "create");
        return "notice/form";
    }

    @GetMapping("/notice/admin/edit/{id}")
    public String editForm(@PathVariable Long id, Model model) {
        NoticeEntity notice = noticeService.get(id);
        model.addAttribute("notice", notice);
        model.addAttribute("mode", "edit");
        return "notice/form";
    }

    @PostMapping("/notice/admin/create")
    public String create(@RequestParam String title,
                         @RequestParam String content,
                         @RequestParam String tag,
                         @RequestParam(defaultValue = "false") boolean pinned,
                         @RequestParam(value = "image", required = false) MultipartFile image,
                         Authentication auth) {
        String authorId = auth != null ? auth.getName() : null;
        NoticeEntity saved = noticeService.create(title, content, tag, pinned, authorId, image);
        return "redirect:/notice/view/" + saved.getId();
    }

    @PostMapping("/notice/admin/update/{id}")
    public String update(@PathVariable Long id,
                         @RequestParam String title,
                         @RequestParam String content,
                         @RequestParam String tag,
                         @RequestParam(defaultValue = "false") boolean pinned,
                         @RequestParam(value = "image", required = false) MultipartFile image) {
        NoticeEntity saved = noticeService.update(id, title, content, tag, pinned, image);
        return "redirect:/notice/view/" + saved.getId();
    }

    @PostMapping("/notice/admin/delete/{id}")
    public String delete(@PathVariable Long id) {
        noticeService.delete(id);
        return "redirect:/notice/list";
    }
}
