package io.skymailer.house.service;

import io.skymailer.house.dto.DashboardSummary;
import io.skymailer.house.model.LeadPriority;
import io.skymailer.house.model.LeadStatus;
import io.skymailer.house.model.PropertyStatus;
import io.skymailer.house.repository.LeadRepository;
import io.skymailer.house.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final PropertyRepository propertyRepository;
    private final LeadRepository leadRepository;

    public DashboardSummary getSummary() {
        // Properties by status
        Map<String, Long> propertiesByStatus = new LinkedHashMap<>();
        for (PropertyStatus s : PropertyStatus.values()) {
            propertiesByStatus.put(s.name(), propertyRepository.countByStatus(s));
        }

        // Leads by status
        Map<String, Long> leadsByStatus = new LinkedHashMap<>();
        for (LeadStatus s : LeadStatus.values()) {
            leadsByStatus.put(s.name(), leadRepository.countByStatus(s));
        }

        // Leads by priority
        Map<String, Long> leadsByPriority = new LinkedHashMap<>();
        for (LeadPriority p : LeadPriority.values()) {
            leadsByPriority.put(p.name(), leadRepository.countByPriority(p));
        }

        long totalLeads = leadRepository.count();
        long bookedLeads = leadRepository.countByStatus(LeadStatus.BOOKED);
        double conversionRate = totalLeads > 0 ? (double) bookedLeads / totalLeads * 100 : 0.0;

        return DashboardSummary.builder()
                .propertiesByStatus(propertiesByStatus)
                .leadsByStatus(leadsByStatus)
                .leadsByPriority(leadsByPriority)
                .totalLeads(totalLeads)
                .bookedLeads(bookedLeads)
                .conversionRate(Math.round(conversionRate * 100.0) / 100.0)
                .build();
    }
}

