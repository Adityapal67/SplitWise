package com.splitwise.repo;

import com.splitwise.entity.Expenses;
import com.splitwise.entity.Group;
import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ExpensesRepository extends Repository<Expenses, Long> {
    List<Expenses> findByGroup(Group group);
}