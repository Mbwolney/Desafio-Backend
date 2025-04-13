package com.simplesdental.product.controller;

import com.simplesdental.product.dto.*;
import com.simplesdental.product.model.User;
import com.simplesdental.product.repository.UserRepository;
import com.simplesdental.product.security.auth.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping()
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private JwtUtil jwtUtil;


    @Operation(
            summary = "Realiza login do usuário",
            description = "Autentica o usuário com e-mail e senha, e retorna um token JWT para uso nas requisições autenticadas."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Login realizado com sucesso",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "Token de exemplo",
                                    summary = "JWT gerado após login bem-sucedido",
                                    value = "{\n  \"token\": \"eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJjb250YXRvQHNpbXBsZXNkZW50YWwuY29tIiwiaWQiOjEsInJvbGUiOiJBRE1JTiIsImlhdCI6MTc0NDU1OTIzMiwiZXhwIjoxNzQ0NjQ1NjMyfQ.8FzbSRTJ9F6FTxDB3f4QWXSFwoDe0uuY4Rq8FmBPbU0\"\n}"
                            ),
                            schema = @Schema(implementation = LoginResponse.class)
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "Senha inválida",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(value = "{\"error\": \"Senha inválida\"}")
                    )
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário não encontrado"
            )
    })
    @PostMapping("/auth/login")
    public ResponseEntity<?> login(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Credenciais do usuário para login",
                    required = true,
                    content = @Content(schema = @Schema(implementation = LoginRequest.class),
                            examples = @ExampleObject(
                                    value = """
                {
                  "email": "admin@empresa.com",
                  "password": "senha123456"
                }
                """
                            )
                    )
            )
            @RequestBody LoginRequest loginRequest) {
        logger.info("[UserController:login] Tentativa de login para o e-mail {}", loginRequest.getEmail());

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> {
                    logger.error("[UserController:login] Usuário com e-mail {} não encontrado", loginRequest.getEmail());
                    return new RuntimeException("Usuário não encontrado");
                });

        if (!(loginRequest.getPassword().equals(user.getPassword()))) {
            logger.warn("[UserController:login] Senha inválida para o usuário {}", loginRequest.getEmail());
            return ResponseEntity.status(401).body("Senha inválida");
        }

        String token = jwtUtil.generateToken(
                user.getId(),
                user.getEmail(),
                user.getRole().name()
        );

        logger.info("[UserController:login] Login realizado com sucesso para {}", loginRequest.getEmail());
        return ResponseEntity.ok(new LoginResponse(token));
    }

    @PreAuthorize("hasAnyRole('ADMIN')")
    @PostMapping("/auth/register")
    public ResponseEntity<Void> register(@RequestBody CreateUserDto createUserDto) {
        logger.info("[UserController:register] Registrando novo usuário: {}", createUserDto.email());

        User user = User.builder()
                .name(createUserDto.name())
                .email(createUserDto.email())
                .password(createUserDto.password())
                .role(createUserDto.role())
                .build();
        userRepository.save(user);
        logger.info("[UserController:register] Usuário registrado com sucesso: {}", createUserDto.email());
        return new ResponseEntity<>(HttpStatus.CREATED);
    }

    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @GetMapping("/auth/context")
    @Cacheable(value = "userContextCache", key = "#authentication.name")
    public ResponseEntity<UserResponse> getUserContext(Authentication authentication) {
        String email = authentication.getName();
        logger.info("[UserController:getUserContext] Buscando contexto do usuário: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("[UserController:getUserContext] Usuário com e-mail {} não encontrado", email);
                    return new UsernameNotFoundException("Usuário não encontrado");
                });

        logger.debug("[UserController:getUserContext] Contexto carregado para usuário: {}", email);
        return ResponseEntity.ok(new UserResponse(user.getId(), user.getEmail(), user.getRole()));
    }

    @Operation(
            summary = "Atualizar a senha do usuário logado",
            description = "Permite que o usuário atualize apenas sua própria senha. Requer autenticação com token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Senha atualizada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token inválido ou não enviado"),
            @ApiResponse(responseCode = "404", description = "Usuário não encontrado")
    })
    @PutMapping("/users/password")
    @PreAuthorize("hasAnyRole('ADMIN', 'USER')")
    @CacheEvict(value = "userContextCache", key = "#authentication.name")
    public ResponseEntity<Void> updatePassword(@RequestBody PasswordUpdateDto passwordUpdateDto,
                                               Authentication authentication) {
        String email = authentication.getName();
        logger.info("[UserController:updatePassword] Atualizando senha do usuário: {}", email);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> {
                    logger.error("[UserController:updatePassword] Usuário com e-mail {} não encontrado", email);
                    return new UsernameNotFoundException("Usuário não encontrado");
                });

        user.setPassword(passwordUpdateDto.newPassword());
        userRepository.save(user);

        logger.info("[UserController:updatePassword] Senha atualizada com sucesso para {}", email);
        return ResponseEntity.noContent().build();
    }
}
