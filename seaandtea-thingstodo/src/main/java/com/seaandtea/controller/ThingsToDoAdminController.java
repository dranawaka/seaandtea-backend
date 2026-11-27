package com.seaandtea.controller;

import com.seaandtea.dto.AdminDtos.ActivityInput;
import com.seaandtea.dto.AdminDtos.AdminRow;
import com.seaandtea.dto.AdminDtos.EventInput;
import com.seaandtea.dto.AdminDtos.OccurrenceInput;
import com.seaandtea.dto.AdminDtos.RestaurantInput;
import com.seaandtea.dto.AdminDtos.StatusInput;
import com.seaandtea.entity.Activity;
import com.seaandtea.entity.Event;
import com.seaandtea.entity.EventOccurrence;
import com.seaandtea.entity.Restaurant;
import com.seaandtea.entity.ThingBase;
import com.seaandtea.entity.ThingStatus;
import com.seaandtea.entity.ThingType;
import com.seaandtea.service.ThingsToDoAdminService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Things to do admin", description = "Create and moderate activities, events, and restaurants")
@SecurityRequirement(name = "bearerAuth")
public class ThingsToDoAdminController {

    private final ThingsToDoAdminService adminService;

    @GetMapping("/things-to-do/pending")
    public List<AdminRow> pending() {
        return adminService.pending();
    }

    @GetMapping("/activities")
    public Page<AdminRow> activities(@RequestParam(required = false) ThingStatus status,
                                     @RequestParam(required = false) String q,
                                     @RequestParam(defaultValue = "0") int page,
                                     @RequestParam(defaultValue = "12") int size) {
        return adminService.list(ThingType.ACTIVITY, status, q, page, size);
    }

    @GetMapping("/events")
    public Page<AdminRow> events(@RequestParam(required = false) ThingStatus status,
                                 @RequestParam(required = false) String q,
                                 @RequestParam(defaultValue = "0") int page,
                                 @RequestParam(defaultValue = "12") int size) {
        return adminService.list(ThingType.EVENT, status, q, page, size);
    }

    @GetMapping("/restaurants")
    public Page<AdminRow> restaurants(@RequestParam(required = false) ThingStatus status,
                                      @RequestParam(required = false) String q,
                                      @RequestParam(defaultValue = "0") int page,
                                      @RequestParam(defaultValue = "12") int size) {
        return adminService.list(ThingType.RESTAURANT, status, q, page, size);
    }

    @GetMapping("/activities/{id}")
    public Activity activity(@PathVariable Long id) {
        return adminService.getActivity(id);
    }

    @PostMapping("/activities")
    public ResponseEntity<Activity> createActivity(@RequestBody ActivityInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createActivity(input));
    }

    @PutMapping("/activities/{id}")
    public Activity updateActivity(@PathVariable Long id, @RequestBody ActivityInput input) {
        return adminService.updateActivity(id, input);
    }

    @DeleteMapping("/activities/{id}")
    public ResponseEntity<Void> deleteActivity(@PathVariable Long id) {
        adminService.deleteActivity(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/events/{id}")
    public Event event(@PathVariable Long id) {
        return adminService.getEvent(id);
    }

    @PostMapping("/events")
    public ResponseEntity<Event> createEvent(@RequestBody EventInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createEvent(input));
    }

    @PutMapping("/events/{id}")
    public Event updateEvent(@PathVariable Long id, @RequestBody EventInput input) {
        return adminService.updateEvent(id, input);
    }

    @DeleteMapping("/events/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable Long id) {
        adminService.deleteEvent(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/events/{id}/occurrences")
    public ResponseEntity<EventOccurrence> addOccurrence(@PathVariable Long id, @RequestBody OccurrenceInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.addOccurrence(id, input));
    }

    @PutMapping("/event-occurrences/{id}")
    public EventOccurrence updateOccurrence(@PathVariable Long id, @RequestBody OccurrenceInput input) {
        return adminService.updateOccurrence(id, input);
    }

    @DeleteMapping("/event-occurrences/{id}")
    public ResponseEntity<Void> deleteOccurrence(@PathVariable Long id) {
        adminService.deleteOccurrence(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/restaurants/{id}")
    public Restaurant restaurant(@PathVariable Long id) {
        return adminService.getRestaurant(id);
    }

    @PostMapping("/restaurants")
    public ResponseEntity<Restaurant> createRestaurant(@RequestBody RestaurantInput input) {
        return ResponseEntity.status(HttpStatus.CREATED).body(adminService.createRestaurant(input));
    }

    @PutMapping("/restaurants/{id}")
    public Restaurant updateRestaurant(@PathVariable Long id, @RequestBody RestaurantInput input) {
        return adminService.updateRestaurant(id, input);
    }

    @DeleteMapping("/restaurants/{id}")
    public ResponseEntity<Void> deleteRestaurant(@PathVariable Long id) {
        adminService.deleteRestaurant(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{type}/{id}/status")
    public ThingBase status(@PathVariable String type, @PathVariable Long id,
                            @RequestBody StatusInput input, Authentication authentication) {
        return adminService.setStatus(ThingsToDoAdminService.parseType(type), id, input, authentication);
    }
}
