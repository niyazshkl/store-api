package com.codewithmosh.store.products;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("products")
@AllArgsConstructor
public class ProductController {
    private final ProductRepository productRepository;
    private final ProductMapper productMapper;
    private final CategoryRepository categoryRepository;

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

    @PostMapping("")
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto, UriComponentsBuilder uriComponentsBuilder){
        Category category = categoryRepository.findById((productDto.getCategoryId())).orElse(null);
        if (category==null) return ResponseEntity.badRequest().build();
        Product product = productMapper.toEntity(productDto);
        product.setCategory(category);
        productRepository.save(product);
        URI uri =  uriComponentsBuilder.path("/products/{id}").buildAndExpand(product.getId()).toUri();
        return ResponseEntity.created(uri).body(productDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(@PathVariable Long id, @RequestBody ProductDto productDto){
        Product product = productRepository.findById(id).orElse(null);
        if (product==null) return ResponseEntity.notFound().build();
        Category category = categoryRepository.findById(productDto.getCategoryId()).orElse(null);
        if (category==null) return ResponseEntity.notFound().build();
        productMapper.update(productDto,product);
        product.setCategory(category);
        productRepository.save(product);
        return ResponseEntity.ok(productDto);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id){
        Product product = productRepository.findById(id).orElse(null);
        if (product==null) return ResponseEntity.notFound().build();
        productRepository.delete(product);
        return ResponseEntity.noContent().build();
    }

}
