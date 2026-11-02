package com.codewithmosh.store.controllers;

import com.codewithmosh.store.dtos.ProductDto;
import com.codewithmosh.store.entities.Product;
import com.codewithmosh.store.mappers.ProductMapper;
import com.codewithmosh.store.repositories.ProductRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.server.DelegatingServerHttpResponse;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("products")
@AllArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    @GetMapping("")
    public ResponseEntity<List<ProductDto>> getAllProducts(@RequestParam(name = "categoryId", required = false, defaultValue = "-1") Long categoryId ){
        List<Product> products;
        if (categoryId!=-1)
            products = productRepository.getByCategoryId(categoryId);
        else
            products = productRepository.findAll();
        List<ProductDto> productDtos = new ArrayList<>();
                products.stream().forEach(
                        product -> {
                            productDtos.add(productMapper.toDto(product));
                        }
        );
        return ResponseEntity.ok(productDtos);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<ProductDto> getById(@PathVariable(name = "productId") Long productId){
        Product product = productRepository.findById(productId).orElse(null);
        if (product==null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(productMapper.toDto(product));
    }
}
