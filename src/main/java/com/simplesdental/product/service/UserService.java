package com.simplesdental.product.service;

import com.simplesdental.product.dto.LoginRequest;
import com.simplesdental.product.dto.LoginResponse;
import com.simplesdental.product.model.User;
import com.simplesdental.product.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public LoginResponse loginResponse(LoginRequest loginRequest){
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado"));
        if (!user.getPassword().equals(loginRequest.getPassword())) {
            throw new RuntimeException("Senha inválida");
        }

        return new LoginResponse(user.getId(), user.getEmail(), user.getRole());
    }

    public User save(User user) {
        return userRepository.save(user);
    }
}
