package com.splitwise.repo;

import com.splitwise.entity.Group;
import com.splitwise.entity.GroupMember;
import com.splitwise.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.Repository;

import java.util.List;
import java.util.Optional;

public interface GroupMemberRepository extends JpaRepository<GroupMember, Long> {
    List<GroupMember> findByGroup(Group group);
    boolean existsByGroupAndUser(Group group, User user);
    Optional<GroupMember> findByGroupAndUser(Group group, User user);

    List<GroupMember> findByUser(User currentUser);
}