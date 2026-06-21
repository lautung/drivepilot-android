package com.lautung.phonecar.backend.content;

import com.lautung.phonecar.backend.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/discovery/contents")
public class AdminContentController {
    private final ContentService service;
    public AdminContentController(ContentService service) { this.service = service; }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) ContentService.ContentView create(@Valid @RequestBody ContentRequest r) { return service.create(r.toService()); }
    @GetMapping PageResponse<ContentService.ContentView> list(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) { return service.adminList(page, size); }
    @PutMapping("/{id}") ContentService.ContentView update(@PathVariable UUID id, @Valid @RequestBody ContentRequest r) { return service.update(id, r.toService()); }
    @PostMapping("/{id}/publish") ContentService.ContentView publish(@PathVariable UUID id) { return service.publish(id); }
    @PostMapping("/{id}/unpublish") ContentService.ContentView unpublish(@PathVariable UUID id) { return service.unpublish(id); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) void delete(@PathVariable UUID id) { service.delete(id); }
    public record ContentRequest(@NotNull ContentCategory category, @NotBlank @Size(max = 160) String title,
            @NotBlank @Size(max = 500) String summary, @NotBlank String body, UUID mediaId) {
        ContentService.ContentRequest toService() { return new ContentService.ContentRequest(category, title, summary, body, mediaId); }
    }
}
