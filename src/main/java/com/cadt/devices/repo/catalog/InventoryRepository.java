package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.Inventory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends JpaRepository<Inventory, String> {

    Optional<Inventory> findByVariantId(String variantId);

    @Query("SELECT i FROM Inventory i WHERE i.quantity <= i.safetyStock")
    List<Inventory> findLowStockItems();

    @Query("SELECT i FROM Inventory i WHERE (i.quantity - i.reserved) <= 0")
    List<Inventory> findOutOfStockItems();

    @Query("SELECT COUNT(i) FROM Inventory i WHERE (i.quantity - i.reserved) > 0")
    long countInStockItems();
}
