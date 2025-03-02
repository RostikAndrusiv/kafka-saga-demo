package com.rostik.andrusiv.product.service;

import com.rostik.andrusiv.core.dto.Product;
import com.rostik.andrusiv.core.exception.ProductInsufficientQuantityException;
import com.rostik.andrusiv.core.exception.ProductNotFoundException;
import com.rostik.andrusiv.product.jpa.entity.ProductEntity;
import com.rostik.andrusiv.product.jpa.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class ProductServiceImpl implements ProductService {
    private final ProductRepository productRepository;

    public ProductServiceImpl(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Override
    public Product reserve(Product desiredProduct, UUID orderId) {
        log.info("Attempting to reserve Product ID: {}, Quantity: {} for Order ID: {}",
                desiredProduct.getId(), desiredProduct.getQuantity(), orderId);

        ProductEntity productEntity = productRepository.findById(desiredProduct.getId())
                .orElseThrow(() -> new ProductNotFoundException(desiredProduct.getId().toString()));

        if (desiredProduct.getQuantity() > productEntity.getQuantity()) {
            log.error("Insufficient quantity for Product ID: {}, Requested: {}, Available: {}",
                    productEntity.getId(), desiredProduct.getQuantity(), productEntity.getQuantity());
            throw new ProductInsufficientQuantityException(productEntity.getId(), orderId);
        }

        productEntity.setQuantity(productEntity.getQuantity() - desiredProduct.getQuantity());
        productRepository.save(productEntity);

        log.info("Successfully reserved Product ID: {}, Remaining Quantity: {}",
                productEntity.getId(), productEntity.getQuantity());

        var reservedProduct = new Product();
        BeanUtils.copyProperties(productEntity, reservedProduct);
        reservedProduct.setQuantity(desiredProduct.getQuantity());
        return reservedProduct;
    }

    @Override
    public void cancelReservation(Product productToCancel, UUID orderId) {
        log.info("Attempting to cancel reservation for Product ID: {}, Quantity: {} for Order ID: {}",
                productToCancel.getId(), productToCancel.getQuantity(), orderId);

        ProductEntity productEntity = productRepository.findById(productToCancel.getId())
                .orElseThrow(() -> new ProductNotFoundException(productToCancel.getId().toString()));

        productEntity.setQuantity(productEntity.getQuantity() + productToCancel.getQuantity());
        productRepository.save(productEntity);

        log.info("Successfully cancelled reservation for Product ID: {}, New Quantity: {}",
                productEntity.getId(), productEntity.getQuantity());
    }

    @Override
    public Product save(Product product) {
        log.info("Saving new Product: {}", product);

        ProductEntity productEntity = new ProductEntity();
        BeanUtils.copyProperties(product, productEntity);
        productRepository.save(productEntity);

        Product productReturn = new Product();
        BeanUtils.copyProperties(productEntity, productReturn);
        log.info("Product saved with ID: {}", productReturn.getId());

        return productReturn;
    }

    @Override
    public List<Product> findAll() {
        log.info("Fetching all products.");
        return productRepository.findAll().stream()
                .map(entity -> new Product(entity.getId(), entity.getName(), entity.getPrice(), entity.getQuantity()))
                .collect(Collectors.toList());
    }
}
