package com.splitwise.security;

import com.splitwise.entity.User;
import com.splitwise.repo.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SecurityUtil {
    private final UserRepository userRepository;

    public User getLoggedInUser(){
        String email = SecurityContextHolder.getContext().
                getAuthentication().getName();
         return userRepository.findByEmail(email).orElseThrow(()->
                 new RuntimeException("Logged in user not found "+email));
    }
}
