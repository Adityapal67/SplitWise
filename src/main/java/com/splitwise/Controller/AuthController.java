package com.splitwise.Controller;

import com.splitwise.DTO.AuthResponse;
import com.splitwise.DTO.LoginRequest;
import com.splitwise.DTO.RegisterRequest;
import com.splitwise.entity.User;
import com.splitwise.repo.UserRepository;
import com.splitwise.security.JwtUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

private final UserRepository userRepository;
private final PasswordEncoder passwordEncoder;
private final JwtUtil jwtUtil;
private final AuthenticationManager authenticationManager;

@PostMapping("/register")
public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request){
    System.out.println(">>> REGISTER ENDPOINT HIT");
    if(userRepository.existsByEmail(request.getEmail())){
        return  ResponseEntity.badRequest().body("User is already Exist");
    }
    User user = User.builder()
            .name(request.getName())
            .email(request.getEmail())
            .password(passwordEncoder.encode(request.getPassword()))
            .build();

    userRepository.save(user);
    String token = jwtUtil.generateToken(request.getEmail());
    return ResponseEntity.ok(new AuthResponse(token,user.getEmail(), user.getName()));
}
@PostMapping("/login")
public ResponseEntity<?> login(@Valid @RequestBody LoginRequest request) {

   User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

   if(user == null){
       return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email");
   }

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body("Invalid email or password");
    }

    String token = jwtUtil.generateToken(user.getEmail());
    return ResponseEntity.ok(new AuthResponse(token, user.getEmail(), user.getName()));
}
}
