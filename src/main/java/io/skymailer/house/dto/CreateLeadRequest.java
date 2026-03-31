package io.skymailer.house.dto;

import io.skymailer.house.model.LeadPriority;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateLeadRequest {

    @NotBlank(message = "Buyer name is required")
    private String buyerName;

    @NotBlank(message = "Phone is required")
    @Pattern(regexp = "^\\+?[1-9]\\d{6,14}$", message = "Phone must be a valid phone number (e.g., +1234567890)")
    private String phone;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be a valid email address")
    private String email;

    @NotNull(message = "Property ID is required")
    private Long propertyId;

    @NotNull(message = "Priority is required")
    private LeadPriority priority;

    private String notes;
}

