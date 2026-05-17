package com.splitwise.services;

import com.splitwise.DTO.SettleRequest;
import com.splitwise.DTO.SettlementResponse;
import com.splitwise.DTO.SimplifiedDebt;
import com.splitwise.Exception.ResourceNotFoundException;
import com.splitwise.entity.*;
import com.splitwise.repo.*;
import com.splitwise.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SettlementService {
    private final GroupRepository groupRepository;
    private final SecurityUtil securityUtil;
    private final GroupMemberRepository groupMemberRepository;
    private final ExpensesRepository expensesRepository;
    private final ExpensesSplitRepository expensesSplit;
    private final SettlementRepository settlementRepository;
    private final UserRepository userRepository;


    //Ok in this i approach with greedy method
    //First i mapped
     public List<SimplifiedDebt> getSimplifiedDebts(Long groupID){
         Group group = groupRepository.findById(groupID).orElseThrow(
                 ()-> new ResourceNotFoundException("Group does not exist")
         );
         User user = securityUtil.getLoggedInUser();
         if(!groupMemberRepository.existsByGroupAndUser(group, user)){
             throw new RuntimeException("You are not the member of the group");
         }
         Map<String, BigDecimal> netBal = new HashMap<>();

         List<GroupMember> members = groupMemberRepository.findByGroup(group);
         for(GroupMember member:members){
             netBal.put(member.getUser().getName(),BigDecimal.ZERO);
         }
         List<Expenses> expenses = expensesRepository.findByGroup(group);
         for(Expenses expense:expenses){

             List<ExpensesSplit> splits = expensesSplit.findByExpenses(expense);
             String paidBy = expense.getPaidBy().getName();

             for(ExpensesSplit split:splits){

              if(split.getIsSettled()) continue;

              String oweeName = split.getUser().getName();
              BigDecimal amount = split.getAmountOwed();

              netBal.merge(paidBy,amount,BigDecimal::add);

              netBal.merge(oweeName,amount.negate(),BigDecimal::add);
             }
         }
         List<Settlement> pastSettlements = settlementRepository.findByGroup(group);
         for (Settlement s : pastSettlements) {
             netBal.merge(s.getPaidBy().getName(),
                     s.getAmount().negate(), BigDecimal::add); // paidBy loses credit
             netBal.merge(s.getPaidTo().getName(),
                     s.getAmount(), BigDecimal::add);           // paidTo gains credit
         }


         // Use PriorityQueues for greedy matching
         // MaxHeap for creditors (biggest creditor first)
         PriorityQueue<Map.Entry<String, BigDecimal>> creditors =
                 new PriorityQueue<>(
                         (a, b) -> b.getValue().compareTo(a.getValue()));

         // MaxHeap for debtors (biggest debt first — stored as positive for easy math)
         PriorityQueue<Map.Entry<String, BigDecimal>> debtors =
                 new PriorityQueue<>(
                         (a, b) -> b.getValue().compareTo(a.getValue()));

         for (Map.Entry<String, BigDecimal> entry : netBal.entrySet()) {
             BigDecimal balance = entry.getValue()
                     .setScale(2, RoundingMode.HALF_UP);

             if (balance.compareTo(new BigDecimal("0.01")) > 0) {
                 // Positive balance = creditor (is owed money)
                 creditors.offer(Map.entry(entry.getKey(), balance));
             } else if (balance.compareTo(new BigDecimal("-0.01")) < 0) {
                 // Negative balance = debtor (owes money) — store as positive
                 debtors.offer(Map.entry(entry.getKey(), balance.abs()));
             }

         }

         // Step 3 — Greedy matching: pair largest debtor with largest creditor
         List<SimplifiedDebt> result = new ArrayList<>();

         while (!debtors.isEmpty() && !creditors.isEmpty()) {
             Map.Entry<String, BigDecimal> debtor   = debtors.poll();
             Map.Entry<String, BigDecimal> creditor = creditors.poll();

             BigDecimal debtAmount   = debtor.getValue();
             BigDecimal creditAmount = creditor.getValue();

             // The transaction amount is the smaller of the two
             BigDecimal transactionAmount = debtAmount.min(creditAmount);

             result.add(SimplifiedDebt.builder()
                     .fromUser(debtor.getKey())
                     .toUser(creditor.getKey())
                     .amount(transactionAmount.setScale(2, RoundingMode.HALF_UP))
                     .build());

             // Recalculate remaining balances after this transaction
             BigDecimal remainingDebt   = debtAmount.subtract(transactionAmount);
             BigDecimal remainingCredit = creditAmount.subtract(transactionAmount);

             // If debtor still owes more, put them back
             if (remainingDebt.compareTo(new BigDecimal("0.01")) > 0) {
                 debtors.offer(Map.entry(debtor.getKey(), remainingDebt));
             }

             // If creditor is still owed more, put them back
             if (remainingCredit.compareTo(new BigDecimal("0.01")) > 0) {
                 creditors.offer(Map.entry(creditor.getKey(), remainingCredit));
             }
         }
         return result;
     }
    // ─── RECORD A SETTLEMENT ───────────────────────────────────────────────────
    @Transactional
    public SettlementResponse settle(SettleRequest request) {
        User currentUser = securityUtil.getLoggedInUser();

        Group group = groupRepository.findById(request.getGroupId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group not found: " + request.getGroupId()));

        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        User paidTo = userRepository.findById(request.getPaidToUserId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found: " + request.getPaidToUserId()));

        if (currentUser.getId().equals(paidTo.getId())) {
            throw new RuntimeException("You cannot settle with yourself");
        }

        // Save the settlement record
        Settlement settlement = Settlement.builder()
                .group(group)
                .paidBy(currentUser)
                .paidTo(paidTo)
                .amount(request.getAmount())
                .build();

        settlement = settlementRepository.save(settlement);

        // Mark related expense splits as settled
        markSplitsAsSettled(group, currentUser, paidTo, request.getAmount());

        return toSettlementResponse(settlement);
    }

    // ─── GET SETTLEMENT HISTORY ────────────────────────────────────────────────
    public List<SettlementResponse> getGroupSettlementHistory(Long groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group not found: " + groupId));

        User currentUser = securityUtil.getLoggedInUser();
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }

        return settlementRepository.findByGroup(group).stream()
                .map(this::toSettlementResponse)
                .collect(Collectors.toList());
    }

    // ─── PRIVATE HELPERS ───────────────────────────────────────────────────────
    private void markSplitsAsSettled(Group group, User payer,
                                     User receiver, BigDecimal amount) {
        // Find all unsettled splits where payer owes receiver
        List<Expenses> receiverExpenses = expensesRepository.findBypaidBy(receiver);
        BigDecimal remaining = amount;

        for (Expenses expense : receiverExpenses) {
            if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;

            if (!expense.getGroup().getId().equals(group.getId())) continue;

            List<ExpensesSplit> splits = expensesSplit.findByExpenses(expense);
            for (ExpensesSplit split : splits) {
                if (remaining.compareTo(BigDecimal.ZERO) <= 0) break;
                if (split.getUser().getId().equals(payer.getId())
                        && !split.getIsSettled()) {
                    split.setIsSettled(true);
                    expensesSplit.save(split);
                    remaining = remaining.subtract(split.getAmountOwed());
                }
            }
        }
    }

    private SettlementResponse toSettlementResponse(Settlement s) {
        return SettlementResponse.builder()
                .id(s.getId())
                .paidBy(s.getPaidBy().getName())
                .paidTo(s.getPaidTo().getName())
                .amount(s.getAmount())
                .settledAt(s.getSettledAt())
                .build();
    }

}
