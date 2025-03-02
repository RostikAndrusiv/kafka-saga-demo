package com.rostik.andrusiv.product.controller;

import com.rostik.andrusiv.core.dto.Product;
import com.rostik.andrusiv.product.dto.ProductCreationRequest;
import com.rostik.andrusiv.product.dto.ProductCreationResponse;
import com.rostik.andrusiv.product.service.ProductService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/products")
public class ProductsController {
    private final ProductService productService;

    public ProductsController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public List<Product> findAll() {
        log.info("Fetching all products");
        List<Product> products = productService.findAll();
        log.info("Found {} products", products.size());
        return products;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductCreationResponse save(@RequestBody @Valid ProductCreationRequest request) {
        log.info("Creating product with details: {}", request);

        var product = new Product();
        BeanUtils.copyProperties(request, product);

        Product result = productService.save(product);

        var productCreationResponse = new ProductCreationResponse();
        BeanUtils.copyProperties(result, productCreationResponse);

        log.info("Product created successfully with ID: {}", result.getId());

        return productCreationResponse;
    }
}
