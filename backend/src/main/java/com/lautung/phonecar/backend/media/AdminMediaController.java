package com.lautung.phonecar.backend.media;

import com.lautung.phonecar.backend.common.PageResponse;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/admin/media")
public class AdminMediaController {
    private final MediaService service;
    private final MediaAssetRepository media;
    public AdminMediaController(MediaService service, MediaAssetRepository media) { this.service = service; this.media = media; }
    @PostMapping(consumes = "multipart/form-data") @ResponseStatus(HttpStatus.CREATED)
    MediaResponse upload(@AuthenticationPrincipal Jwt jwt, @RequestPart("file") MultipartFile file) { return response(service.upload(UUID.fromString(jwt.getSubject()), file)); }
    @GetMapping @Transactional(readOnly = true)
    PageResponse<MediaResponse> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        if (page < 0 || size < 1 || size > 100) throw new com.lautung.phonecar.backend.common.ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Invalid pagination");
        return PageResponse.from(media.findAll(PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"))), this::response);
    }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable UUID id) { service.delete(id); }
    private MediaResponse response(MediaAssetEntity e) { return new MediaResponse(e.getId(), e.getOriginalFilename(), e.getContentType(), e.getSizeBytes(), e.getSha256(), e.getStatus().name(), e.getCreatedAt()); }
    public record MediaResponse(UUID id, String originalFilename, String contentType, long sizeBytes, String sha256, String status, java.time.Instant createdAt) {}
}
