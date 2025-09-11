package com.cadt.devices.repo.order;

import com.cadt.devices.model.order.Order;
import com.cadt.devices.model.order.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    List<OrderItem> findByOrderOrderByCreatedAtAsc(Order order);

    void deleteByOrder(Order order);
}
