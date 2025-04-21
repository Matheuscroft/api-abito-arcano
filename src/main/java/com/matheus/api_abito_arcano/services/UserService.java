package com.matheus.api_abito_arcano.services;

import com.matheus.api_abito_arcano.models.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class UserService {

    public User getUsuarioAutenticado() {
        return (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }

}
