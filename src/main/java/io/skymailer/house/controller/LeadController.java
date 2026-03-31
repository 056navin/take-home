package io.skymailer.house.controller;

import io.skymailer.house.dto.CreateLeadRequest;
import io.skymailer.house.dto.TransitionRequest;
import io.skymailer.house.dto.UpdateLeadRequest;
import io.skymailer.house.model.Lead;
import io.skymailer.house.model.LeadPriority;
import io.skymailer.house.model.LeadStatus;
import io.skymailer.house.service.LeadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Lead management and status transitions")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @Operation(summary = "Create a lead", description = "Property must exist and be 'Available'. Rejects duplicate phone+property.")
    public ResponseEntity<?> createLead(@Valid @RequestBody CreateLeadRequest request) {
        Lead lead = leadService.createLead(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(toLeadResponse(lead));
    }

    @GetMapping
    @Operation(summary = "List leads", description = "Filter by status, priority, property_id. Paginated.")
    public ResponseEntity<?> listLeads(
            @RequestParam(required = false) LeadStatus status,
            @RequestParam(required = false) LeadPriority priority,
            @RequestParam(name = "property_id", required = false) Long propertyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(leadService.listLeads(status, priority, propertyId, page, limit)
                .map(this::toLeadResponse));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get single lead", description = "Returns lead with its property details")
    public ResponseEntity<?> getLead(@PathVariable Long id) {
        Lead lead = leadService.getLeadById(id);
        Map<String, Object> response = toLeadResponse(lead);

        // Include property details
        Map<String, Object> propertyDetails = new HashMap<>();
        propertyDetails.put("id", lead.getProperty().getId());
        propertyDetails.put("title", lead.getProperty().getTitle());
        propertyDetails.put("address", lead.getProperty().getAddress());
        propertyDetails.put("city", lead.getProperty().getCity());
        propertyDetails.put("price", lead.getProperty().getPrice());
        propertyDetails.put("bedrooms", lead.getProperty().getBedrooms());
        propertyDetails.put("status", lead.getProperty().getStatus());
        response.put("property", propertyDetails);

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update lead", description = "Update priority or notes only — not status")
    public ResponseEntity<?> updateLead(@PathVariable Long id, @RequestBody UpdateLeadRequest request) {
        Lead lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(toLeadResponse(lead));
    }

    @PostMapping("/{id}/transition")
    @Operation(summary = "Transition lead status",
               description = "Pipeline: NEW→CONTACTED→VISITED→BOOKED. LOST from any (except BOOKED). BOOKED & LOST are terminal.")
    public ResponseEntity<?> transitionLead(@PathVariable Long id,
                                            @Valid @RequestBody TransitionRequest request) {
        Lead lead = leadService.transitionLead(id, request.getStatus());
        return ResponseEntity.ok(toLeadResponse(lead));
    }

    private Map<String, Object> toLeadResponse(Lead lead) {
        Map<String, Object> map = new HashMap<>();
        map.put("id", lead.getId());
        map.put("buyerName", lead.getBuyerName());
        map.put("phone", lead.getPhone());
        map.put("email", lead.getEmail());
        map.put("propertyId", lead.getProperty().getId());
        map.put("status", lead.getStatus());
        map.put("priority", lead.getPriority());
        map.put("notes", lead.getNotes());
        map.put("createdAt", lead.getCreatedAt());
        map.put("updatedAt", lead.getUpdatedAt());
        return map;
    }
}

