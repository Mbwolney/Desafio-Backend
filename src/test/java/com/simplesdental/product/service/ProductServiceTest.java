package com.simplesdental.product.service;

import com.simplesdental.product.model.v1.ProductV1;
import com.simplesdental.product.repository.v1.ProductRepositoryV1;
import com.simplesdental.product.service.v1.ProductServiceV1;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTest {

    @Mock
    private ProductRepositoryV1 productRepository;

    @InjectMocks
    private ProductServiceV1 productService;

    private ProductV1 product;

    @BeforeEach
    void setUp() {
        product = new ProductV1();
        product.setId(1L);
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setPrice(new BigDecimal("19.99"));
        product.setStatus(true);
        product.setCode("001");
    }

    @Test
    void shouldSaveProduct() {
        when(productRepository.save(any(ProductV1.class))).thenReturn(product);

        ProductV1 savedProduct = productService.save(product);

        assertThat(savedProduct).isNotNull();
        assertThat(savedProduct.getId()).isEqualTo(1L);
        assertThat(savedProduct.getName()).isEqualTo("Test Product");
        verify(productRepository, times(1)).save(any(ProductV1.class));
    }

//    @Test
//    void shouldGetAllProducts() {
//        when(productRepository.findAll()).thenReturn(Arrays.asList(product));
//
//        List<Product> products = productService.findAll();
//
//        assertThat(products).isNotNull();
//        assertThat(products.size()).isEqualTo(1);
//        verify(productRepository, times(1)).findAll();
//    }

    @Test
    void shouldGetProductById() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        Optional<ProductV1> foundProduct = productService.findById(1L);

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