package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, String> {

    Optional<Category> findBySlug(String slug);

    List<Category> findByParentIdIsNullAndIsActiveTrueOrderBySortOrder();

    List<Category> findByParentIdAndIsActiveTrueOrderBySortOrder(String parentId);

    @Query("SELECT c FROM Category c WHERE c.isActive = true ORDER BY c.sortOrder")
    List<Category> findAllActiveOrderBySortOrder();

    Page<Category> findByIsActiveTrueOrderBySortOrder(Pageable pageable);

    boolean existsBySlug(String slug);
}
