package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    private List<Category> categories = new ArrayList<>();
    private Long categoryId = 1L;
    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public String addCategory(Category category) {
        category.setCategoryId(categoryId++);
        categories.add(category);
        return "Category added successfully";
    }

    @Override
    public String deleteCategory(int categoryId) {

        Category category = categories.stream()
                        .filter(c->c.getCategoryId()==categoryId)
                        .findFirst()
                        .orElseThrow(()-> new ResponseStatusException(HttpStatus.NOT_FOUND,"Resource Not Found"));

        categories.remove(category);
        return  "Category with categoryId: "+categoryId+" deleted successfully...";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Optional<Category> optionalCategory = categories.stream()
                .filter(c->c.getCategoryId().equals(categoryId)).findFirst();

        if(optionalCategory.isPresent()){
            Category updatedExistingCategory = optionalCategory.get();
            updatedExistingCategory.setCategoryName(category.getCategoryName());
            return updatedExistingCategory;
        }
        else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND,"Category Not Found!");
        }
    }
}
