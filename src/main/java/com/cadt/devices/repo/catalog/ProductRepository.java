package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.ConditionGrade;
import com.cadt.devices.model.catalog.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
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

    @Query("SELECT p FROM Product p JOIN ProductVariant v ON p.id = v.productId " +
            "WHERE p.isActive = true AND v.isActive = true " +
            "AND (:categoryId IS NULL OR p.categoryId = :categoryId) " +
            "AND (:brandId IS NULL OR p.brandId = :brandId) " +
            "AND (:conditionGrade IS NULL OR p.conditionGrade = :conditionGrade) " +
            "AND (:minPrice IS NULL OR v.priceSale >= :minPrice) " +
            "AND (:maxPrice IS NULL OR v.priceSale <= :maxPrice)")
    Page<Product> findWithFilters(
            @Param("categoryId") String categoryId,
            @Param("brandId") String brandId,
            @Param("conditionGrade") ConditionGrade conditionGrade,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);

    boolean existsBySlug(String slug);
    
    boolean existsByCategoryId(String categoryId);
}
