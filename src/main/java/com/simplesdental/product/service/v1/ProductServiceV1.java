package com.simplesdental.product.service.v1;

import com.simplesdental.product.model.v1.ProductV1;
import com.simplesdental.product.repository.v1.ProductRepositoryV1;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceV1 {

    private final ProductRepositoryV1 productRepository;

    @Autowired
    public ProductServiceV1(ProductRepositoryV1 productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductV1> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<ProductV1> findById(Long id) {
        return productRepository.findById(id);
    }

    public ProductV1 save(ProductV1 product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}