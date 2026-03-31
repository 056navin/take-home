package io.skymailer.house.dto;

import io.skymailer.house.model.LeadStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TransitionRequest {
    @NotNull(message = "Status is required")
    private LeadStatus status;
}

