package com.simplesdental.product.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.simplesdental.product.dto.LoginRequest;
import com.simplesdental.product.enumType.Role;
import com.simplesdental.product.model.User;
import com.simplesdental.product.repository.UserRepository;
import com.simplesdental.product.security.auth.JwtUtil;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testLoginSuccess() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@empresa.com", "senha123");

        User mockUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@empresa.com")
                .password("senha123")
                .role(Role.ADMIN)
                .build();

        String fakeJwt = "fake.jwt.token";

        Mockito.when(userRepository.findByEmail("admin@empresa.com"))
                .thenReturn(Optional.of(mockUser));
        Mockito.when(jwtUtil.generateToken(1L, "admin@empresa.com", "ADMIN"))
                .thenReturn(fakeJwt);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(fakeJwt));
    }

    @Test
    void testLoginUserNotFound() throws Exception {
        LoginRequest loginRequest = new LoginRequest("notfound@empresa.com", "senha");

        Mockito.when(userRepository.findByEmail("notfound@empresa.com"))
                .thenReturn(Optional.empty());

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testLoginInvalidPassword() throws Exception {
        LoginRequest loginRequest = new LoginRequest("admin@empresa.com", "wrongPassword");

        User mockUser = User.builder()
                .id(1L)
                .name("Admin")
                .email("admin@empresa.com")
                .password("senha123") // senha verdadeira
                .role(Role.ADMIN)
                .build();

        Mockito.when(userRepository.findByEmail("admin@empresa.com"))
                .thenReturn(Optional.of(mockUser));

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Senha inválida"));
    }
}
