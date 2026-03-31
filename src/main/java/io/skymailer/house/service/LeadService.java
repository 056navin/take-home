package io.skymailer.house.service;

import io.skymailer.house.dto.CreateLeadRequest;
import io.skymailer.house.dto.UpdateLeadRequest;
import io.skymailer.house.exception.DuplicateLeadException;
import io.skymailer.house.exception.InvalidTransitionException;
import io.skymailer.house.model.*;
import io.skymailer.house.repository.LeadRepository;
import io.skymailer.house.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class LeadService {

    private final LeadRepository leadRepository;
    private final PropertyRepository propertyRepository;


    private static final Map<LeadStatus, LeadStatus> PIPELINE_NEXT = Map.of(
            LeadStatus.NEW, LeadStatus.CONTACTED,
            LeadStatus.CONTACTED, LeadStatus.VISITED,
            LeadStatus.VISITED, LeadStatus.BOOKED
    );

    @Transactional
    public Lead createLead(CreateLeadRequest request) {

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "Property not found with id: " + request.getPropertyId()));


        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new IllegalArgumentException(
                    "Cannot create lead for property with status '" + property.getStatus() +
                    "'. Property must be 'AVAILABLE'.");
        }


        if (leadRepository.existsByPhoneAndPropertyId(request.getPhone(), request.getPropertyId())) {
            throw new DuplicateLeadException(
                    "A lead with phone '" + request.getPhone() +
                    "' already exists for property id " + request.getPropertyId());
        }

        Lead lead = Lead.builder()
                .buyerName(request.getBuyerName())
                .phone(request.getPhone())
                .email(request.getEmail())
                .property(property)
                .status(LeadStatus.NEW)
                .priority(request.getPriority())
                .notes(request.getNotes())
                .build();

        return leadRepository.save(lead);
    }

    public Page<Lead> listLeads(LeadStatus status, LeadPriority priority, Long propertyId, int page, int limit) {
        return leadRepository.findAllWithFilters(status, priority, propertyId, PageRequest.of(page, limit));
    }

    public Lead getLeadById(Long id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Lead not found with id: " + id));
    }

    @Transactional
    public Lead updateLead(Long id, UpdateLeadRequest request) {
        Lead lead = getLeadById(id);

        if (request.getPriority() != null) {
            lead.setPriority(request.getPriority());
        }
        if (request.getNotes() != null) {
            lead.setNotes(request.getNotes());
        }

        return leadRepository.save(lead);
    }


    @Transactional
    public Lead transitionLead(Long id, LeadStatus targetStatus) {
        Lead lead = getLeadById(id);
        LeadStatus currentStatus = lead.getStatus();


        if (currentStatus == LeadStatus.BOOKED) {
            throw new InvalidTransitionException(
                    "Cannot transition from 'BOOKED'. It is a terminal status — no further transitions allowed.");
        }
        if (currentStatus == LeadStatus.LOST) {
            throw new InvalidTransitionException(
                    "Cannot transition from 'LOST'. It is a terminal status — no further transitions allowed.");
        }


        if (targetStatus == LeadStatus.LOST) {
            lead.setStatus(LeadStatus.LOST);
            return leadRepository.save(lead);
        }


        LeadStatus expectedNext = PIPELINE_NEXT.get(currentStatus);
        if (expectedNext == null) {
            throw new InvalidTransitionException(
                    "Cannot transition from '" + currentStatus + "'. No valid next status exists.");
        }

        if (targetStatus != expectedNext) {
            throw new InvalidTransitionException(
                    "Cannot move from '" + currentStatus + "' to '" + targetStatus +
                    "'. Next valid status is '" + expectedNext + "'.");
        }

        lead.setStatus(targetStatus);
        Lead savedLead = leadRepository.save(lead);


        if (targetStatus == LeadStatus.BOOKED) {
            Property property = lead.getProperty();
            property.setStatus(PropertyStatus.BOOKED);
            propertyRepository.save(property);
        }

        return savedLead;
    }
}

