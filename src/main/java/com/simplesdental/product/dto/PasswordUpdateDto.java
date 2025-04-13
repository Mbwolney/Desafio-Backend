package com.simplesdental.product.dto;

import io.swagger.v3.oas.annotations.media.Schema;

public record PasswordUpdateDto(
        @Schema(description = "Nova senha do usuário, no mínimo 8 caracteres", example = "NovaSenha123")
        String newPassword
) {
}
