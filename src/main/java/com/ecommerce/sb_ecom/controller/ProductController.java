package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.config.AppConstants;
import com.ecommerce.sb_ecom.model.Product;
import com.ecommerce.sb_ecom.payload.ProductDTO;
import com.ecommerce.sb_ecom.payload.ProductResponse;
import com.ecommerce.sb_ecom.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {

    @Autowired
    ProductService  productService;


    @PostMapping("admin/categories/{categoryId}/product")
    public ResponseEntity<ProductDTO> addProduct(@Valid @RequestBody ProductDTO productDTO,
                                                 @PathVariable Long categoryId)
    {
        ProductDTO savedProductDTO = productService.addProduct(categoryId,productDTO);
        return new ResponseEntity<>(savedProductDTO, HttpStatus.CREATED);
    }

    @GetMapping("public/products")
    public ResponseEntity<ProductResponse> getAllProducts( @RequestParam(name="pageNumber",required = false,defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                           @RequestParam(name="pageSize",required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                           @RequestParam(name="sortBy",required = false,defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                           @RequestParam(name="sortOrder",required = false,defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        ProductResponse productResponse = productService.getAllProducts(pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.OK);
    }

    @GetMapping("public/categories/{categoryId}/products")
    public ResponseEntity<ProductResponse> getAllProductsByCategory(@PathVariable Long categoryId, @RequestParam(name="pageNumber",required = false,defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                    @RequestParam(name="pageSize",required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                    @RequestParam(name="sortBy",required = false,defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                    @RequestParam(name="sortOrder",required = false,defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        ProductResponse productResponse = productService.getAllProductsByCategory(categoryId,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @GetMapping("public/products/keyword/{keyword}")
    public ResponseEntity<ProductResponse> getAllProductsByKeyword(@PathVariable String keyword,@RequestParam(name="pageNumber",required = false,defaultValue = AppConstants.PAGE_NUMBER) Integer pageNumber,
                                                                   @RequestParam(name="pageSize",required = false, defaultValue = AppConstants.PAGE_SIZE) Integer pageSize,
                                                                   @RequestParam(name="sortBy",required = false,defaultValue = AppConstants.SORT_PRODUCTS_BY) String sortBy,
                                                                   @RequestParam(name="sortOrder",required = false,defaultValue = AppConstants.SORT_DIR) String sortOrder) {
        ProductResponse productResponse = productService.getAllProductsByKeyword(keyword,pageNumber,pageSize,sortBy,sortOrder);
        return new ResponseEntity<>(productResponse, HttpStatus.FOUND);
    }

    @PutMapping("admin/products/{productId}")
    public ResponseEntity<ProductDTO> updateProduct(@PathVariable Long productId,
                                                    @Valid @RequestBody ProductDTO productDTO) {
        ProductDTO updatedProductDTO = productService.updateProduct(productId,productDTO);
        return new ResponseEntity<>(updatedProductDTO, HttpStatus.OK);
    }

    @DeleteMapping("admin/products/{productId}")
    public ResponseEntity<ProductDTO> deleteProduct(@PathVariable Long productId) {
        ProductDTO deletedProductDTO = productService.deleteProduct(productId);
        return new ResponseEntity<>(deletedProductDTO, HttpStatus.OK);
    }

    @PutMapping("admin/products/{productId}/image")
    public ResponseEntity<ProductDTO> uploadProductImage(@PathVariable Long productId,
                                                         @RequestParam("image") MultipartFile image) {
        ProductDTO productDTO = productService.uploadProductImage(productId,image);
        return new ResponseEntity<>(productDTO, HttpStatus.OK);
    }
}
