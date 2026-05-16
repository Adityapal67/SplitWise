package com.splitwise.repo;

import com.splitwise.entity.Expenses;
import com.splitwise.entity.ExpensesSplit;
import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;

public interface ExpensesSplitRepository extends JpaRepository<ExpensesSplit, Long> {
    List<ExpensesSplit> findByUserAndIsSettled(User user, Boolean isSettled);
    List<ExpensesSplit> findByExpenses(Expenses expense);
}