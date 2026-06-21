package com.lautung.phonecar.backend.content;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.common.PageResponse;
import com.lautung.phonecar.backend.media.MediaAssetEntity;
import com.lautung.phonecar.backend.media.MediaAssetRepository;
import com.lautung.phonecar.backend.media.MediaService;
import java.time.Instant;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContentService {
    private final DiscoveryContentRepository contents;
    private final ContentFollowRepository follows;
    private final MediaAssetRepository media;
    private final MediaService mediaService;

    public ContentService(DiscoveryContentRepository contents, ContentFollowRepository follows,
            MediaAssetRepository media, MediaService mediaService) {
        this.contents = contents; this.follows = follows; this.media = media; this.mediaService = mediaService;
    }

    @Transactional(readOnly = true)
    public PageResponse<ContentView> published(UUID userId, ContentCategory category, int page, int size) {
        PageRequest pageable = pageRequest(page, size, "publishedAt");
        Page<DiscoveryContentEntity> result = category == null
                ? contents.findAllByStatus(ContentStatus.PUBLISHED, pageable)
                : contents.findAllByStatusAndCategory(ContentStatus.PUBLISHED, category, pageable);
        return PageResponse.from(result, content -> view(content, userId));
    }

    @Transactional(readOnly = true)
    public ContentView publishedOne(UUID userId, UUID id) {
        DiscoveryContentEntity content = require(id);
        if (content.getStatus() != ContentStatus.PUBLISHED) throw notFound();
        return view(content, userId);
    }

    @Transactional
    public void follow(UUID userId, UUID contentId) {
        DiscoveryContentEntity content = require(contentId);
        if (content.getStatus() != ContentStatus.PUBLISHED) throw notFound();
        ContentFollowId id = new ContentFollowId(userId, contentId);
        if (!follows.existsById(id)) follows.save(new ContentFollowEntity(userId, contentId, Instant.now()));
    }

    @Transactional
    public void unfollow(UUID userId, UUID contentId) { follows.deleteById(new ContentFollowId(userId, contentId)); }

    @Transactional
    public ContentView create(ContentRequest request) {
        Instant now = Instant.now();
        return view(contents.save(new DiscoveryContentEntity(UUID.randomUUID(), request.category(), request.title().trim(),
                request.summary().trim(), request.body().trim(), media(request.mediaId()), now)), null);
    }

    @Transactional
    public ContentView update(UUID id, ContentRequest request) {
        DiscoveryContentEntity content = require(id);
        content.update(request.category(), request.title().trim(), request.summary().trim(), request.body().trim(), media(request.mediaId()), Instant.now());
        return view(contents.save(content), null);
    }

    @Transactional
    public ContentView publish(UUID id) { DiscoveryContentEntity c = require(id); c.publish(Instant.now()); return view(contents.save(c), null); }
    @Transactional
    public ContentView unpublish(UUID id) { DiscoveryContentEntity c = require(id); c.unpublish(Instant.now()); return view(contents.save(c), null); }
    @Transactional
    public void delete(UUID id) { contents.delete(require(id)); }

    @Transactional(readOnly = true)
    public PageResponse<ContentView> adminList(int page, int size) {
        return PageResponse.from(contents.findAll(pageRequest(page, size, "createdAt")), content -> view(content, null));
    }

    private DiscoveryContentEntity require(UUID id) { return contents.findById(id).orElseThrow(this::notFound); }
    private MediaAssetEntity media(UUID id) { if (id == null) return null; return media.findById(id).orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "MEDIA_NOT_FOUND", "Media does not exist")); }
    private ApiException notFound() { return new ApiException(HttpStatus.NOT_FOUND, "CONTENT_NOT_FOUND", "Discovery content does not exist"); }
    private PageRequest pageRequest(int page, int size, String sort) { if (page < 0 || size < 1 || size > 100) throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Page must be non-negative and size between 1 and 100"); return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, sort)); }
    private ContentView view(DiscoveryContentEntity c, UUID userId) { return new ContentView(c.getId(), c.getCategory(), c.getTitle(), c.getSummary(), c.getBody(), mediaService.signed(c.getMedia()), c.getStatus(), c.getPublishedAt(), userId != null && follows.existsById(new ContentFollowId(userId, c.getId())), c.getCreatedAt(), c.getUpdatedAt()); }

    public record ContentRequest(ContentCategory category, String title, String summary, String body, UUID mediaId) {}
    public record ContentView(UUID id, ContentCategory category, String title, String summary, String body,
            MediaService.SignedMedia media, ContentStatus status, Instant publishedAt, boolean followed,
            Instant createdAt, Instant updatedAt) {}
}
