package com.lautung.phonecar.backend.content;

import com.lautung.phonecar.backend.common.PageResponse;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/discovery/contents")
public class DiscoveryContentController {
    private final ContentService service;
    public DiscoveryContentController(ContentService service) { this.service = service; }

    @GetMapping
    PageResponse<ContentService.ContentView> list(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(required = false) ContentCategory category,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return service.published(userId(jwt), category, page, size);
    }
    @GetMapping("/{id}") ContentService.ContentView get(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { return service.publishedOne(userId(jwt), id); }
    @PutMapping("/{id}/follow") @ResponseStatus(HttpStatus.NO_CONTENT) void follow(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { service.follow(userId(jwt), id); }
    @DeleteMapping("/{id}/follow") @ResponseStatus(HttpStatus.NO_CONTENT) void unfollow(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) { service.unfollow(userId(jwt), id); }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
}
