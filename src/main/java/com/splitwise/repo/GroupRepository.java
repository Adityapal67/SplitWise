package com.splitwise.repo;

import com.splitwise.entity.Group;
import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface GroupRepository extends JpaRepository<Group, Long> {
    List<Group> findByCreatedBy(User user);
}