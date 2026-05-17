package com.splitwise.Controller;

import com.splitwise.DTO.AddExpenseRequest;
import com.splitwise.DTO.BalanceResponse;
import com.splitwise.DTO.ExpenseResponse;
import com.splitwise.services.ExpenseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/expenses")
@RequiredArgsConstructor
public class ExpenseController {
    private final ExpenseService expenseService;

    @PostMapping
    public ResponseEntity<ExpenseResponse> addExpense(
            @Valid @RequestBody AddExpenseRequest request) {
        return ResponseEntity.ok(expenseService.addExpense(request));
    }

    @GetMapping("/group/{groupId}")
    public ResponseEntity<List<ExpenseResponse>> getGroupExpenses(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }

    @GetMapping("/my-balances")
    public ResponseEntity<BalanceResponse> getMyBalances() {
        return ResponseEntity.ok(expenseService.getMyBalances());
    }
}
