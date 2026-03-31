package io.skymailer.house.dto;

import lombok.Builder;
import lombok.Data;
import java.util.Map;

@Data
@Builder
public class DashboardSummary {
    private Map<String, Long> propertiesByStatus;
    private Map<String, Long> leadsByStatus;
    private Map<String, Long> leadsByPriority;
    private long totalLeads;
    private long bookedLeads;
    private double conversionRate;
}

