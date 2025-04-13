package com.simplesdental.product.repository.v2;

import com.simplesdental.product.model.v2.ProductV2;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepositoryV2 extends JpaRepository<ProductV2, Long> {
}