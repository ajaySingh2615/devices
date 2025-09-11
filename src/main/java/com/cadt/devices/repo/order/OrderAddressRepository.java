package com.cadt.devices.repo.order;

import com.cadt.devices.model.order.Order;
import com.cadt.devices.model.order.OrderAddress;
import com.cadt.devices.model.order.AddressType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderAddressRepository extends JpaRepository<OrderAddress, String> {

    List<OrderAddress> findByOrder(Order order);

    Optional<OrderAddress> findByOrderAndType(Order order, AddressType type);

    void deleteByOrder(Order order);
}
