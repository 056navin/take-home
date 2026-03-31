package io.skymailer.house.controller;

import io.skymailer.house.model.PropertyStatus;
import io.skymailer.house.service.PropertyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/properties")
@RequiredArgsConstructor
@Tag(name = "Properties", description = "Read-only property endpoints")
public class PropertyController {

    private final PropertyService propertyService;

    @GetMapping
    @Operation(summary = "List all properties", description = "Supports filtering by city, status, bedrooms. Paginated.")
    public ResponseEntity<?> listProperties(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) PropertyStatus status,
            @RequestParam(required = false) Integer bedrooms,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(propertyService.listProperties(city, status, bedrooms, page, limit));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single property", description = "Returns property details with lead counts grouped by status")
    public ResponseEntity<Map<String, Object>> getProperty(@PathVariable Long id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }
}

