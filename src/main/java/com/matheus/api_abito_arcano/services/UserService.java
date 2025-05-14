package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.dtos.response.UserResponseDTO;
import com.matheus.api_abito_arcano.models.User;
import com.matheus.api_abito_arcano.repositories.AreaRepository;
import com.matheus.api_abito_arcano.repositories.DayRepository;
import com.matheus.api_abito_arcano.repositories.TarefaRepository;
import com.matheus.api_abito_arcano.repositories.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;


@Service
public class UserService {

    @Autowired
    DayRepository dayRepository;

    @Autowired
    TarefaRepository tarefaRepository;

    @Autowired
    AreaRepository areaRepository;

    @Autowired
    UserRepository userRepository;


    public User getUsuarioAutenticado() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

    public List<UserResponseDTO> getAllUsers() {
        return userRepository.findAll().stream()
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getLogin(),
                        user.getRole().name()
                ))
                .toList();
    }

    public UserResponseDTO getUserById(UUID id) {
        return userRepository.findById(id)
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getLogin(),
                        user.getRole().name()
                ))
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public void deleteUserAccount(UUID userId) {
        userRepository.deleteById(userId);
    }

}
