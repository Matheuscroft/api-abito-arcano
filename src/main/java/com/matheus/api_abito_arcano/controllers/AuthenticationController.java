package com.matheus.api_abito_arcano.controllers;


import com.matheus.api_abito_arcano.dtos.AuthenticationDTO;
import com.matheus.api_abito_arcano.dtos.RegisterDTO;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.UserRepository;
import com.matheus.api_abito_arcano.services.AuthService;
import com.matheus.api_abito_arcano.services.TokenService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("auth")
public class AuthenticationController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private AuthService authService;

    @PostMapping("/login")
    public ResponseEntity login(@RequestBody @Valid AuthenticationDTO authenticationDTO){
        UsernamePasswordAuthenticationToken credential = new UsernamePasswordAuthenticationToken(authenticationDTO.login(), authenticationDTO.password());
        Authentication authenticate = authenticationManager.authenticate(credential);

        String token = tokenService.generateToken((User) authenticate.getPrincipal());

        return ResponseEntity.ok(token);
    }

    @PostMapping("/register")
    public ResponseEntity register(@RequestBody @Valid RegisterDTO registerDTO){
        if(userRepository.findByLogin(registerDTO.email()) != null){
            return ResponseEntity.badRequest().build();
        }

        authService.registerUser(registerDTO);

        return ResponseEntity.ok().build();
    }
}