package com.ecommerce.sb_ecom.controller;

import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api")
public class CategoryController {

    @Autowired
    CategoryService  categoryService;

    @GetMapping("/public/categories")
    public ResponseEntity<List<Category>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        return ResponseEntity.status(HttpStatus.OK).body(categories);
    }

    @PostMapping("/public/categories")
    public ResponseEntity<String> addCategory(@RequestBody Category category) {
        String status=categoryService.addCategory(category);
       return new ResponseEntity<>(status,HttpStatus.CREATED) ;
    }

    @PutMapping("/public/categories/{categoryId}")
    public ResponseEntity<String> updateCategory(@PathVariable Long categoryId,@RequestBody Category category) {
        try{
            Category updateCategory = categoryService.updateCategory(category,categoryId);
            return ResponseEntity.ok("Category with CategoryId: "+categoryId+" updated Successfully...");
        }
        catch(ResponseStatusException exception){
            return new ResponseEntity<>(exception.getReason(),exception.getStatusCode());
        }

    }

    @DeleteMapping("/public/categories/{categoryId}")
    public ResponseEntity<String> deleteCategory(@PathVariable("categoryId") int categoryId) {
        
        try{
            String status = categoryService.deleteCategory(categoryId);
            return ResponseEntity.ok(status);
        }
        catch(ResponseStatusException exception){
            return new ResponseEntity<>(exception.getReason(),exception.getStatusCode());
        }
      
    }
}
