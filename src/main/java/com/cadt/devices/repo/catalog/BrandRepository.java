package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BrandRepository extends JpaRepository<Brand, String> {

    Optional<Brand> findBySlug(String slug);

    List<Brand> findByIsActiveTrueOrderByName();
    
    List<Brand> findAllByOrderByName();

    Page<Brand> findByIsActiveTrueOrderByName(Pageable pageable);

    boolean existsBySlug(String slug);
}
