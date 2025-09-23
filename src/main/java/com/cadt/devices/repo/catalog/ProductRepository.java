package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, String> {

    Optional<Product> findBySlug(String slug);

    Page<Product> findByIsActiveTrueOrderByCreatedAtDesc(Pageable pageable);

    Page<Product> findByCategoryIdAndIsActiveTrueOrderByCreatedAtDesc(String categoryId, Pageable pageable);

    Page<Product> findByBrandIdAndIsActiveTrueOrderByCreatedAtDesc(String brandId, Pageable pageable);

    @Query("SELECT p FROM Product p WHERE p.isActive = true AND " +
            "(LOWER(p.title) LIKE LOWER(CONCAT('%', :query, '%')) OR " +
            "LOWER(p.description) LIKE LOWER(CONCAT('%', :query, '%')))")
    Page<Product> searchProducts(@Param("query") String query, Pageable pageable);

    @Query("SELECT DISTINCT p FROM Product p JOIN ProductVariant v ON p.id = v.productId " +
            "WHERE p.isActive = true AND v.isActive = true " +
            "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
            "AND (:brandId IS NULL OR p.brandId = :brandId) " +
            "AND (:conditionGrade IS NULL OR p.conditionGrade = :conditionGrade) " +
            "AND (:minPrice IS NULL OR v.priceSale >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.priceSale <= :maxPrice) " +
            "AND (:cpuVendor IS NULL OR v.cpuVendor = :cpuVendor) " +
            "AND (:cpuSeries IS NULL OR LOWER(v.cpuSeries) = LOWER(:cpuSeries)) " +
            "AND (:cpuGeneration IS NULL OR LOWER(v.cpuGeneration) = LOWER(:cpuGeneration)) " +
            "AND (:operatingSystem IS NULL OR v.operatingSystem = :operatingSystem) " +
            "AND (:touchscreen IS NULL OR v.touchscreen = :touchscreen) " +
            "AND (:useCase IS NULL OR v.useCase = :useCase)")
    Page<Product> findWithFilters(
            @Param("categoryId") String categoryId,
            @Param("brandId") String brandId,
            @Param("conditionGrade") ConditionGrade conditionGrade,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("cpuVendor") ProcessorVendor cpuVendor,
            @Param("cpuSeries") String cpuSeries,
            @Param("cpuGeneration") String cpuGeneration,
            @Param("operatingSystem") OperatingSystem operatingSystem,
            @Param("touchscreen") Boolean touchscreen,
            @Param("useCase") UseCase useCase,
            Pageable pageable);

    Page<Product> findByIsActiveTrueAndIsBestsellerTrueOrderByCreatedAtDesc(Pageable pageable);

    boolean existsBySlug(String slug);
    
    boolean existsByCategoryId(String categoryId);
    
    // Admin dashboard queries
    long countByIsActiveTrue();
    
    long countByCreatedAtAfter(Instant date);
    
    long countByCreatedAtBetween(Instant start, Instant end);
    
    @Query("SELECT p FROM Product p WHERE p.createdAt > :date ORDER BY p.createdAt DESC")
    List<Product> findByCreatedAtAfterOrderByCreatedAtDesc(@Param("date") Instant date);
}
