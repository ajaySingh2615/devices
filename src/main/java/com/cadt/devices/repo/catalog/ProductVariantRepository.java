package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, String> {

    Optional<ProductVariant> findBySku(String sku);

    List<ProductVariant> findByProductIdAndIsActiveTrueOrderByCreatedAt(String productId);

    @Query("SELECT v FROM ProductVariant v JOIN Inventory i ON v.id = i.variantId " +
            "WHERE v.productId = :productId AND v.isActive = true AND i.quantity > 0")
    List<ProductVariant> findInStockByProductId(String productId);

    boolean existsBySku(String sku);
}
