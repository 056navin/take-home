package io.skymailer.house.repository;

import io.skymailer.house.model.Property;
import io.skymailer.house.model.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    @Query("SELECT p FROM Property p WHERE " +
           "(:city IS NULL OR p.city = :city) AND " +
           "(:status IS NULL OR p.status = :status) AND " +
           "(:bedrooms IS NULL OR p.bedrooms = :bedrooms)")
    Page<Property> findAllWithFilters(
            @Param("city") String city,
            @Param("status") PropertyStatus status,
            @Param("bedrooms") Integer bedrooms,
            Pageable pageable);

    long countByStatus(PropertyStatus status);
}

