package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.RoleRequest;
import com.lankafreshmart.market_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoleRequestRepository extends JpaRepository<RoleRequest, Long> {
    List<RoleRequest> findByStatus(String status);
    List<RoleRequest> findByUser(User user);
}
