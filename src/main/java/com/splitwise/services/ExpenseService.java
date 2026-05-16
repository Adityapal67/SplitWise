package com.splitwise.services;

import com.splitwise.DTO.AddExpenseRequest;
import com.splitwise.DTO.BalanceResponse;
import com.splitwise.DTO.ExpenseResponse;
import com.splitwise.DTO.GroupResponse;
import com.splitwise.Exception.ResourceNotFoundException;
import com.splitwise.entity.*;
import com.splitwise.repo.*;
import com.splitwise.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ExpenseService {
    private final ExpensesRepository expensesRepository;
    private final ExpensesSplitRepository expensesSplitRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserRepository userRepository;
    private final SecurityUtil securityUtil;

    @Transactional
    public ExpenseResponse addExpense(AddExpenseRequest request){
        User user = securityUtil.getLoggedInUser();
        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(()->new RuntimeException("Group not found "+request.getGroupId()));

        if(!groupMemberRepository.existsByGroupAndUser(group,user)){
            throw new RuntimeException("You are not member of this group");
        }
       Expenses expenses = Expenses.builder()
                .amount(request.getAmount())
                .split_type(request.getSplitType())
                .group(group)
                .paidBy(user)
                .description(request.getDescription())
                .build();
       expenses = expensesRepository.save(expenses);
        // Calculate and save the splits
        List<ExpensesSplit> splits = calculateSplit(expenses, request, group);
        expensesSplitRepository.saveAll(splits);

        return expenseResponse(expenses, splits);
    }

    @Transactional
    public List<ExpenseResponse> getGroupExpenses(Long groupId) {
        User currentUser = securityUtil.getLoggedInUser();

        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group not found: " + groupId));

        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        List<Expenses> expenses = expensesRepository.findByGroup(group);

        return expenses.stream()
                .map(e -> expenseResponse(e,
                        expensesSplitRepository.findByExpenses(e)))
                .collect(Collectors.toList());
    }
    public BalanceResponse getMyBalances() {
        User currentUser = securityUtil.getLoggedInUser();

        // All unsettled splits where I owe someone
        List<ExpensesSplit> iOwe = expensesSplitRepository
                .findByUserAndIsSettled(currentUser, false);

        // All unsettled splits where someone owes me
        // (expenses I paid, where the split user is not me)
        List<Expenses> myPaidExpenses = expensesRepository.findBypaidBy(currentUser);

        Map<String, BigDecimal> balanceMap = new HashMap<>();

        // What I owe others (negative from my perspective)
        for (ExpensesSplit split : iOwe) {
            String paidByName = split.getExpenses().getPaidBy().getName();
            if (!paidByName.equals(currentUser.getName())) {
                balanceMap.merge(paidByName,
                        split.getAmountOwed().negate(), BigDecimal::add);
            }
        }

        // What others owe me (positive from my perspective)
        for (Expenses expense : myPaidExpenses) {
            List<ExpensesSplit> splits = expensesSplitRepository.findByExpenses(expense);
            for (ExpensesSplit split : splits) {
                if (!split.getUser().getId().equals(currentUser.getId())
                        && !split.getIsSettled()) {
                    balanceMap.merge(split.getUser().getName(),
                            split.getAmountOwed(), BigDecimal::add);
                }
            }
        }

        // Build response
        List<BalanceResponse.BalanceDetail> details = balanceMap.entrySet().stream()
                .map(e -> BalanceResponse.BalanceDetail.builder()
                        .withUser(e.getKey())
                        .amount(e.getValue())
                        .build())
                .collect(Collectors.toList());

        BigDecimal totalOwe = balanceMap.values().stream()
                .filter(v -> v.compareTo(BigDecimal.ZERO) < 0)
                .map(BigDecimal::abs)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOwed = balanceMap.values().stream()
                .filter(v -> v.compareTo(BigDecimal.ZERO) > 0)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return BalanceResponse.builder()
                .totalYouOwe(totalOwe)
                .totalOwedToYou(totalOwed)
                .balanceDetails(details)
                .build();
    }

    //-----------Split Algorithm--------------
    private List<ExpensesSplit> calculateSplit(Expenses expenses,
                                              AddExpenseRequest request,
                                              Group group){
        List<ExpensesSplit> expensesSplits = new ArrayList<>();

        switch (request.getSplitType()){
            case EQUAL -> {
                List<GroupMember> members = groupMemberRepository.findByGroup(group);
                int memberCount = members.size();
                BigDecimal splitExpense = request.getAmount().
                        divide(new BigDecimal(memberCount),2, RoundingMode.HALF_UP);
                for(GroupMember member:members){
                    expensesSplits.add(ExpensesSplit.builder()
                            .user(member.getUser())
                            .amountOwed(splitExpense)
                            .isSettled(false)
                            .expenses(expenses)
                            .build());
                }
            }
            case EXACT -> {
                if(request.getSplits() == null || request.getSplits().isEmpty())
                    throw new RuntimeException(
                            "Split are required for exact Split type");

                BigDecimal total = request.getSplits().stream()
                        .map(AddExpenseRequest.SplitDetails::getValue)
                        .reduce(BigDecimal.ZERO , BigDecimal::add);

                if(total.subtract(request.getAmount()).abs()
                        .compareTo(new BigDecimal("0.01"))>0){
                    throw new RuntimeException(
                            "Split amounts must add up to total: " + request.getAmount()
                            + " total amount now is: " + total
                    );
                }
                for(AddExpenseRequest.SplitDetails details : request.getSplits()){
                    User user = userRepository.findById(details.getUserId()).
                            orElseThrow(()-> new RuntimeException(
                                    "User not found " + details.getUserId()));

                    expensesSplits.add(ExpensesSplit.builder()
                            .expenses(expenses)
                            .user(user)
                            .amountOwed(details.getValue())
                            .isSettled(false)
                            .build());
                }

            }
            case PERCENTAGE -> {
                if(request.getSplits() == null || request.getSplits().isEmpty())
                    throw new RuntimeException(
                            "Split are required for percentage Split type");

                BigDecimal total = request.getSplits().stream()
                        .map(AddExpenseRequest.SplitDetails::getValue)
                        .reduce(BigDecimal.ZERO , BigDecimal::add);

                if(total.compareTo(new BigDecimal("100")) != 0){
                    throw new RuntimeException(
                            "Percentages must add up to 100. Currently: " + total);
                }
                for(AddExpenseRequest.SplitDetails details : request.getSplits()){
                    User user = userRepository.findById(details.getUserId()).
                            orElseThrow(()-> new RuntimeException(
                                    "User not found " + details.getUserId()));
                    BigDecimal owed = request.getAmount()
                                    .multiply(details.getValue())
                               .divide(new BigDecimal("100"),2,RoundingMode.HALF_UP);

                    expensesSplits.add(ExpensesSplit.builder()
                            .expenses(expenses)
                            .user(user)
                            .amountOwed(owed)
                            .isSettled(false)
                            .build());
                }

            }
        }
        return expensesSplits;
    }
    //--------Mapper-------
    private ExpenseResponse expenseResponse(Expenses expenses,
                                            List<ExpensesSplit> expensesSplits){
        List<ExpenseResponse.SplitDetail> splitDetails = expensesSplits.stream()
                .map(s -> ExpenseResponse.SplitDetail.builder()
                        .userId(s.getUser().getId())
                        .userName(s.getUser().getName())
                        .amountOwed(s.getAmountOwed())
                        .isSettled(s.getIsSettled())
                        .build())
                .toList();
        return ExpenseResponse.builder()
                .id(expenses.getId())
                .description(expenses.getDescription())
                .amount(expenses.getAmount())
                .splitType(expenses.getSplit_type())
                .paidBy(expenses.getPaidBy().getName())
                .createdAt(expenses.getCreatedAt())
                .splits(splitDetails)
                .build();
    }

}
