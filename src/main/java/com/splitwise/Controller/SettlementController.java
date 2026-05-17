package com.splitwise.Controller;

import com.splitwise.DTO.SettleRequest;
import com.splitwise.DTO.SettlementResponse;
import com.splitwise.DTO.SimplifiedDebt;
import com.splitwise.services.SettlementService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/settlement")
@RequiredArgsConstructor
public class SettlementController {
    private final SettlementService settlementService;

    // GET /api/settlements/group/{id}/simplify
    // Returns the minimum transactions needed to settle all debts
    @GetMapping("/group/{groupId}/simplify")
    public ResponseEntity<List<SimplifiedDebt>> getSimplifiedDebts(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(
                settlementService.getSimplifiedDebts(groupId));
    }

    // POST /api/settlements/settle
    // Record that you paid someone
    @PostMapping("/settle")
    public ResponseEntity<SettlementResponse> settle(
            @Valid @RequestBody SettleRequest request) {
        return ResponseEntity.ok(settlementService.settle(request));
    }

    // GET /api/settlements/group/{id}/history
    // All past settlements in the group
    @GetMapping("/group/{groupId}/history")
    public ResponseEntity<List<SettlementResponse>> getHistory(
            @PathVariable Long groupId) {
        return ResponseEntity.ok(
                settlementService.getGroupSettlementHistory(groupId));
    }
}
