package com.simplesdental.product.dto.v1;

import com.simplesdental.product.model.v1.ProductV1;

import java.math.BigDecimal;

public class ProductV1Dto {
    private Long id;
    private String code;
    private String name;
    private String description;
    private BigDecimal price;
    private Boolean status;
    private Long categoryId;

    public static ProductV1Dto fromEntity(ProductV1 product) {
        ProductV1Dto dto = new ProductV1Dto();
        dto.setId(product.getId());
        dto.setCode(String.format("PROD-%03d", product.getCode()));
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setStatus(product.getStatus());
        dto.setCategoryId(product.getCategory().getId());
        return dto;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Boolean getStatus() {
        return status;
    }

    public void setStatus(Boolean status) {
        this.status = status;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
}
