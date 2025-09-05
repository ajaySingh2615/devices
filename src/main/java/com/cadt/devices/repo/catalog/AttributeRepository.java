package com.cadt.devices.repo.catalog;

import com.cadt.devices.model.catalog.Attribute;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AttributeRepository extends JpaRepository<Attribute, String> {
    Optional<Attribute> findByCode(String code);

    List<Attribute> findByIsFilterableTrue();

    boolean existsByCode(String code);
}
