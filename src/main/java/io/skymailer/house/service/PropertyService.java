package io.skymailer.house.service;

import io.skymailer.house.model.Property;
import io.skymailer.house.model.PropertyStatus;
import io.skymailer.house.repository.LeadRepository;
import io.skymailer.house.repository.PropertyRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final LeadRepository leadRepository;

    public Page<Property> listProperties(String city, PropertyStatus status, Integer bedrooms, int page, int limit) {
        return propertyRepository.findAllWithFilters(city, status, bedrooms, PageRequest.of(page, limit));
    }

    public Map<String, Object> getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Property not found with id: " + id));

        List<Object[]> leadCounts = leadRepository.countByPropertyIdGroupByStatus(id);
        Map<String, Long> leadsByStatus = new HashMap<>();
        for (Object[] row : leadCounts) {
            leadsByStatus.put(row[0].toString(), (Long) row[1]);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("id", property.getId());
        result.put("title", property.getTitle());
        result.put("address", property.getAddress());
        result.put("city", property.getCity());
        result.put("price", property.getPrice());
        result.put("bedrooms", property.getBedrooms());
        result.put("status", property.getStatus());
        result.put("createdAt", property.getCreatedAt());
        result.put("leadsByStatus", leadsByStatus);

        return result;
    }
}

