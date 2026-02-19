package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.repositories.CategoryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    CategoryRepository categoryRepository;;

    @Override
    public List<Category> getAllCategories() {
        List<Category> categories = categoryRepository.findAll();
        if(categories.isEmpty()){
            throw new APIException("No category created till now!!!");
        }
        return categories;
    }

    @Override
    public String addCategory(Category category) {

        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(optionalCategory.isPresent()){
            throw new APIException("Category with name "+category.getCategoryName()+" already exists!!!");
        }

        category.setCategoryId(null);
        categoryRepository.save(category);
        return "Category added successfully";
    }

    @Override
    public String deleteCategory(Long categoryId) {

        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        categoryRepository.delete(categoryToDelete);
        return  "Category with categoryId: "+categoryId+" deleted successfully...";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {

        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(optionalCategory.isPresent()){
            throw new APIException("Category with name "+category.getCategoryName()+" already exists!!!");
        }

        category.setCategoryId(categoryId);
        Category updatedCategory = categoryRepository.save(category);
        return updatedCategory;
    }
}
