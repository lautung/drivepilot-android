package com.lautung.phonecar.backend.user;

import com.lautung.phonecar.backend.common.ApiException;
import java.time.Instant;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me/preferences")
public class UserPreferencesController {
    private final UserPreferencesRepository preferences;

    public UserPreferencesController(UserPreferencesRepository preferences) { this.preferences = preferences; }

    @GetMapping
    @Transactional(readOnly = true)
    PreferencesResponse get(@AuthenticationPrincipal Jwt jwt) { return response(require(jwt)); }

    @PatchMapping
    @Transactional
    PreferencesResponse patch(@AuthenticationPrincipal Jwt jwt, @RequestBody PreferencesPatch patch) {
        UserPreferencesEntity entity = require(jwt);
        entity.apply(patch, Instant.now());
        return response(preferences.save(entity));
    }

    private UserPreferencesEntity require(Jwt jwt) {
        return preferences.findById(UUID.fromString(jwt.getSubject())).orElseThrow(() ->
                new ApiException(HttpStatus.NOT_FOUND, "PREFERENCES_NOT_FOUND", "User preferences do not exist"));
    }

    private PreferencesResponse response(UserPreferencesEntity entity) {
        return new PreferencesResponse(entity.isLocationSharingEnabled(), entity.isCabinCameraEnabled(), entity.getUpdatedAt());
    }

    public record PreferencesPatch(Boolean locationSharingEnabled, Boolean cabinCameraEnabled) {}
    public record PreferencesResponse(boolean locationSharingEnabled, boolean cabinCameraEnabled, Instant updatedAt) {}
}
