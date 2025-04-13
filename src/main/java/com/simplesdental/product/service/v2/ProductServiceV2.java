package com.simplesdental.product.service.v2;

import com.simplesdental.product.model.v2.ProductV2;
import com.simplesdental.product.repository.v2.ProductRepositoryV2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class ProductServiceV2 {

    private final ProductRepositoryV2 productRepository;

    @Autowired
    public ProductServiceV2(ProductRepositoryV2 productRepository) {
        this.productRepository = productRepository;
    }

    public Page<ProductV2> findAll(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Optional<ProductV2> findById(Long id) {
        return productRepository.findById(id);
    }

    public ProductV2 save(ProductV2 product) {
        return productRepository.save(product);
    }

    public void deleteById(Long id) {
        productRepository.deleteById(id);
    }
}