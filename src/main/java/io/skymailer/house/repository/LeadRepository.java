package io.skymailer.house.repository;

import io.skymailer.house.model.Lead;
import io.skymailer.house.model.LeadPriority;
import io.skymailer.house.model.LeadStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LeadRepository extends JpaRepository<Lead, Long> {

    @Query("SELECT l FROM Lead l WHERE " +
           "(:status IS NULL OR l.status = :status) AND " +
           "(:priority IS NULL OR l.priority = :priority) AND " +
           "(:propertyId IS NULL OR l.property.id = :propertyId)")
    Page<Lead> findAllWithFilters(
            @Param("status") LeadStatus status,
            @Param("priority") LeadPriority priority,
            @Param("propertyId") Long propertyId,
            Pageable pageable);

    boolean existsByPhoneAndPropertyId(String phone, Long propertyId);

    long countByStatus(LeadStatus status);

    long countByPriority(LeadPriority priority);

    @Query("SELECT l.status, COUNT(l) FROM Lead l WHERE l.property.id = :propertyId GROUP BY l.status")
    List<Object[]> countByPropertyIdGroupByStatus(@Param("propertyId") Long propertyId);
}

