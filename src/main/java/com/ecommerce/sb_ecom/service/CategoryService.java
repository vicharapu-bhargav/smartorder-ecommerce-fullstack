package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.payload.CategoryDTO;
import com.ecommerce.sb_ecom.payload.CategoryResponse;

public interface CategoryService {

    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize,String SortBy, String sortOrder);
    public CategoryDTO createCategory(CategoryDTO category);

    public CategoryDTO deleteCategory(Long categoryId);

    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId);
}
