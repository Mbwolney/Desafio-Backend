package com.simplesdental.product.dto;

import com.simplesdental.product.enumType.Role;

public record CreateUserDto(
        String name,
        String email,
        String password,
        Role role
) {
}
