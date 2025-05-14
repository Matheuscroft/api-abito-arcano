package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.RegisterDTO;
import com.matheus.api_abito_arcano.exceptions.UserAlreadyExistsException;
import com.matheus.api_abito_arcano.models.Area;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AreaRepository areaRepository;

    @Autowired
    private DayService dayService;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByLogin(username);
    }

    public User registerUser(RegisterDTO registerDTO) {
        String encryptedPassword = new BCryptPasswordEncoder().encode(registerDTO.password());
        if (userRepository.existsByLogin(registerDTO.login())) {
            throw new UserAlreadyExistsException(registerDTO.login());
        }


        User user = new User();
        user.setLogin(registerDTO.login());
        user.setPassword(encryptedPassword);
        user.setRole(registerDTO.role());

        user = userRepository.save(user);

        Area area = new Area();
        area.setName("Sem Categoria");
        area.setColor("#000000");
        area.setUser(user);
        areaRepository.save(area);

        dayService.createInitialDaysForUser(user);

        return user;
    }


}