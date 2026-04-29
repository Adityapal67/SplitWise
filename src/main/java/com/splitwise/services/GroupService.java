package com.splitwise.services;

import com.splitwise.DTO.AddMemberRequest;
import com.splitwise.DTO.GroupCreationRequest;
import com.splitwise.DTO.GroupResponse;
import com.splitwise.Exception.ResourceNotFoundException;
import com.splitwise.entity.Group;
import com.splitwise.entity.GroupMember;
import com.splitwise.entity.User;
import com.splitwise.repo.GroupMemberRepository;
import com.splitwise.repo.GroupRepository;
import com.splitwise.repo.UserRepository;
import com.splitwise.security.SecurityUtil;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final SecurityUtil securityUtil;
    private final UserRepository userRepository;

    private GroupResponse toGroupResponse(Group group) {
        List<GroupMember> members = groupMemberRepository.findByGroup(group);

        List<GroupResponse.MemberResponse> memberResponses = members.stream()
                .map(m -> GroupResponse.MemberResponse.builder()
                        .userId(m.getUser().getId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .joined_at(m.getJoinedAt())
                        .build())
                .collect(Collectors.toList());

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .description(group.getDescription())
                .createdBy(group.getCreatedBy().getName())
                .created_at(group.getCreated_at())
                .members(memberResponses)
                .build();
    }
    private Group findGroupOrThrow(Long groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Group not found with ID: " + groupId));
    }

    private void verifyMembership(Group group) {
        User currentUser = securityUtil.getLoggedInUser();
        if (!groupMemberRepository.existsByGroupAndUser(group, currentUser)) {
            throw new RuntimeException("You are not a member of this group");
        }
    }

    @Transactional
    public GroupResponse createGroup(GroupCreationRequest groupCreationRequest){
        User currentUser = securityUtil.getLoggedInUser();
        Group group = Group.builder()
                .name(groupCreationRequest.getGroupName())
                .description(groupCreationRequest.getDescription())
                .createdBy(currentUser)
                .build();
        groupRepository.save(group);
        GroupMember groupMember = GroupMember.builder()
                .group(group)
                .user(currentUser)
                .build();
        groupMemberRepository.save(groupMember);
        return toGroupResponse(group);
    }
    public GroupResponse getGroup(Long groupId){
        Group group = findGroupOrThrow(groupId);
        verifyMembership(group);
        return toGroupResponse(group);
    }

    public List<GroupResponse> getMyGroups() {
        User currentUser = securityUtil.getLoggedInUser();

        // Get all groups where current user is a member
        List<GroupMember> memberships = groupMemberRepository.findByUser(currentUser);

        return memberships.stream()
                .map(m -> toGroupResponse(m.getGroup()))
                .collect(Collectors.toList());
    }
    @Transactional
    public GroupResponse addMember(Long groupId, AddMemberRequest request){
        Group group = findGroupOrThrow(groupId);
        verifyMembership(group);

        User currUser = userRepository.findById(request.getUserId()).orElseThrow(()->
                new RuntimeException("No user Found"));
        if(groupMemberRepository.existsByGroupAndUser(group,currUser)){
            throw new RuntimeException(currUser.getName() + "is already member of group");
        }
        GroupMember groupMember = GroupMember.builder()
                .user(currUser)
                .group(group)
                .build();
        groupMemberRepository.save(groupMember);
        return toGroupResponse(group);
    }
    @Transactional
    public void deleteMember(Long userID,Long groupId){
        Group group = findGroupOrThrow(groupId);
        User currUser = securityUtil.getLoggedInUser();

        if(!group.getCreatedBy().getId().equals(currUser.getId())){
            throw new RuntimeException("Only group admin can remove themselves");
        }
        if(currUser.getId().equals(userID)){
            throw new RuntimeException("User can't delete themselves");
        }
        User userToRemove = userRepository.findById(userID)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found with ID: " + userID));

        GroupMember membership = groupMemberRepository
                .findByGroupAndUser(group, userToRemove)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "This user is not a member of the group"));

        groupMemberRepository.delete(membership);

    }

}
