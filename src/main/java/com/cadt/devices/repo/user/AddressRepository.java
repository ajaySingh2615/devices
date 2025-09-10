package com.cadt.devices.repo.user;

import com.cadt.devices.model.user.Address;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, String> {
    List<Address> findByUserIdOrderByIsDefaultDescCreatedAtDesc(String userId);
    long countByUserId(String userId);
}


