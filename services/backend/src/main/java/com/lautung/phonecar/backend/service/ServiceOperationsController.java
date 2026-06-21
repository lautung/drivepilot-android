package com.lautung.phonecar.backend.service;

import com.lautung.phonecar.backend.common.ApiException;
import com.lautung.phonecar.backend.common.PageResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class ServiceOperationsController {
    private final VehicleReservationRepository reservations;
    private final MaintenanceBookingRepository maintenance;
    private final SubscriptionRepository subscriptions;

    public ServiceOperationsController(VehicleReservationRepository reservations,
            MaintenanceBookingRepository maintenance, SubscriptionRepository subscriptions) {
        this.reservations = reservations; this.maintenance = maintenance; this.subscriptions = subscriptions;
    }

    @PostMapping("/vehicle-reservations")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    ReservationResponse createReservation(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ReservationRequest request) {
        VehicleReservationEntity entity = reservations.save(new VehicleReservationEntity(UUID.randomUUID(), userId(jwt),
                request.paint(), request.wheel(), Instant.now()));
        return reservationResponse(entity);
    }

    @GetMapping("/vehicle-reservations")
    @Transactional(readOnly = true)
    PageResponse<ReservationResponse> reservations(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(reservations.findAllByUserId(userId(jwt), pageRequest(page, size)), this::reservationResponse);
    }

    @PostMapping("/maintenance-bookings")
    @ResponseStatus(HttpStatus.CREATED)
    @Transactional
    MaintenanceResponse createMaintenance(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody MaintenanceRequest request) {
        if (request.bookingDate().isBefore(LocalDate.now())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "BOOKING_DATE_IN_PAST", "Booking date must not be in the past");
        }
        MaintenanceBookingEntity entity = maintenance.save(new MaintenanceBookingEntity(UUID.randomUUID(), userId(jwt),
                request.service(), request.bookingDate(), Instant.now()));
        return maintenanceResponse(entity);
    }

    @GetMapping("/maintenance-bookings")
    @Transactional(readOnly = true)
    PageResponse<MaintenanceResponse> maintenance(@AuthenticationPrincipal Jwt jwt,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size) {
        return PageResponse.from(maintenance.findAllByUserId(userId(jwt), pageRequest(page, size)), this::maintenanceResponse);
    }

    @GetMapping("/subscriptions")
    @Transactional(readOnly = true)
    List<SubscriptionResponse> subscriptions(@AuthenticationPrincipal Jwt jwt) {
        return subscriptions.findAllByUserIdOrderByPlan(userId(jwt)).stream().map(this::subscriptionResponse).toList();
    }

    @PutMapping("/subscriptions/{plan}")
    @Transactional
    SubscriptionResponse activate(@AuthenticationPrincipal Jwt jwt, @PathVariable SubscriptionPlan plan) {
        UUID userId = userId(jwt); Instant now = Instant.now();
        SubscriptionEntity entity = subscriptions.findByUserIdAndPlan(userId, plan)
                .orElseGet(() -> new SubscriptionEntity(UUID.randomUUID(), userId, plan, now));
        entity.activate(now);
        return subscriptionResponse(subscriptions.save(entity));
    }

    @DeleteMapping("/subscriptions/{plan}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Transactional
    void deactivate(@AuthenticationPrincipal Jwt jwt, @PathVariable SubscriptionPlan plan) {
        subscriptions.findByUserIdAndPlan(userId(jwt), plan).ifPresent(entity -> {
            entity.deactivate(Instant.now()); subscriptions.save(entity);
        });
    }

    private PageRequest pageRequest(int page, int size) {
        if (page < 0 || size < 1 || size > 100) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_PAGE", "Page must be non-negative and size must be between 1 and 100");
        }
        return PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
    }
    private UUID userId(Jwt jwt) { return UUID.fromString(jwt.getSubject()); }
    private ReservationResponse reservationResponse(VehicleReservationEntity e) { return new ReservationResponse(e.getId(), e.getPaint(), e.getWheel(), e.getStatus(), e.getCreatedAt()); }
    private MaintenanceResponse maintenanceResponse(MaintenanceBookingEntity e) { return new MaintenanceResponse(e.getId(), e.getService(), e.getBookingDate(), e.getStatus(), e.getCreatedAt()); }
    private SubscriptionResponse subscriptionResponse(SubscriptionEntity e) { return new SubscriptionResponse(e.getId(), e.getPlan(), e.isActive(), e.getActivatedAt(), e.getDeactivatedAt(), e.getUpdatedAt()); }

    public record ReservationRequest(@NotNull PaintOption paint, @NotNull WheelOption wheel) {}
    public record ReservationResponse(UUID id, PaintOption paint, WheelOption wheel, RecordStatus status, Instant createdAt) {}
    public record MaintenanceRequest(@NotNull MaintenanceService service, @NotNull LocalDate bookingDate) {}
    public record MaintenanceResponse(UUID id, MaintenanceService service, LocalDate bookingDate, RecordStatus status, Instant createdAt) {}
    public record SubscriptionResponse(UUID id, SubscriptionPlan plan, boolean active, Instant activatedAt, Instant deactivatedAt, Instant updatedAt) {}
}
