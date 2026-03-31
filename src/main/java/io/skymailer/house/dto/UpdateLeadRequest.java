package io.skymailer.house.dto;

import io.skymailer.house.model.LeadPriority;
import lombok.Data;

@Data
public class UpdateLeadRequest {
    private LeadPriority priority;
    private String notes;
}

