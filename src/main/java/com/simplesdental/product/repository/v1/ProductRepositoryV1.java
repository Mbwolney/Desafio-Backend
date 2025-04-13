package com.simplesdental.product.repository.v1;

import com.simplesdental.product.model.v1.ProductV1;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepositoryV1 extends JpaRepository<ProductV1, Long> {
}