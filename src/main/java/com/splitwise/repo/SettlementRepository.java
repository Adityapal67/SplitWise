package com.splitwise.repo;

import com.splitwise.entity.Group;
import com.splitwise.entity.Settlement;
import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface SettlementRepository extends JpaRepository<Settlement, Long> {
    List<Settlement> findByGroup(Group group);
    List<Settlement> findByPaidByOrPaidTo(User paidBy, User paidTo);
}