package com.splitwise.Controller;

import com.splitwise.DTO.AddMemberRequest;
import com.splitwise.DTO.GroupCreationRequest;
import com.splitwise.DTO.GroupResponse;
import com.splitwise.services.GroupService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/group")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService service;
//    POST /api/groups — create a group
    @PostMapping()
    public ResponseEntity<GroupResponse> create(@Valid @RequestBody GroupCreationRequest request){
        return ResponseEntity.ok(service.createGroup(request));
    }
//    POST /api/ — add a member
    @PostMapping("/{id}/members")
    public ResponseEntity<GroupResponse> addMember(@PathVariable Long userId , @Valid @RequestBody AddMemberRequest request){
        return ResponseEntity.ok(service.addMember(userId,request));
    }
//    GET /api/groups/{id} — get group details + members
    @GetMapping("/{id}")
    public ResponseEntity<GroupResponse> groupDetails(@PathVariable Long groupId){
        return ResponseEntity.ok(service.getGroup(groupId));
    }
//    GET /api/groups/my — all groups you're part of
    @GetMapping("/my")
    public ResponseEntity<List<GroupResponse>> myGroup(){
        return ResponseEntity.ok(service.getMyGroups());
    }
//    DEL /api/groups/} — remove member
    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<String> removeMember(
        @PathVariable Long id,
        @PathVariable Long userId) {
         service.deleteMember(userId , id);
         return ResponseEntity.ok("Member removed successfully");
    }


}
