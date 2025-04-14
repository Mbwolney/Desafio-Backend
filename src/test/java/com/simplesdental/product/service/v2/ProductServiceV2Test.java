package com.simplesdental.product.service.v2;

import com.simplesdental.product.model.v2.ProductV2;
import com.simplesdental.product.repository.v2.ProductRepositoryV2;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceV2Test {

    @Mock
    private ProductRepositoryV2 productRepository;

    @InjectMocks
    private ProductServiceV2 productService;

    private ProductV2 product;

    @BeforeEach
    void setUp() {
        product = new ProductV2();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("19.99"));
        product.setStatus(true);
        product.setCode(1);
    }

    @Test
    void shouldSaveProduct() {
        when(productRepository.save(any(ProductV2.class))).thenReturn(product);

        ProductV2 savedProduct = productService.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo(1L);
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(any(ProductV2.class));
    }

    @Test
    void shouldGetAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<ProductV2> page = new PageImpl<>(List.of(product));

        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<ProductV2> products = productService.findAll(pageable);

        assertThat(products).isNotNull();
        assertThat(products.getContent().size()).isEqualTo(1);
        assertThat(products.getContent().get(0).getName()).isEqualTo("Test Product");

        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductV2> foundProduct = productService.findById(1L);

        assertThat(foundProduct).isPresent();
        assertThat(foundProduct.get().getId()).isEqualTo(1L);
        assertThat(foundProduct.get().getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void shouldDeleteProductById() {
        doNothing().when(productRepository).deleteById(1L);

        productService.deleteById(1L);

        verify(productRepository, times(1)).deleteById(1L);
    }
}