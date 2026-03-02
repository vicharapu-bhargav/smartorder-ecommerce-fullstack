package com.ecommerce.sb_ecom.service;

import com.ecommerce.sb_ecom.exceptions.APIException;
import com.ecommerce.sb_ecom.exceptions.ResourceNotFoundException;
import com.ecommerce.sb_ecom.model.Category;
import com.ecommerce.sb_ecom.payload.CategoryDTO;
import com.ecommerce.sb_ecom.payload.CategoryResponse;
import com.ecommerce.sb_ecom.repositories.CategoryRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CategoryRepository categoryRepository;;

    @Override
    public CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String SortBy,  String sortOrder) {

        Sort sort = sortOrder.equals("asc")
                ? Sort.by(SortBy).ascending()
                : Sort.by(SortBy).descending();

        Pageable pageable = PageRequest.of(pageNumber, pageSize,sort);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);

        List<Category> categories = categoryPage.getContent();
        if(categories.isEmpty()){
            throw new APIException("No category created till now!!!");
        }
        List<CategoryDTO> categoryDTOList = categories.stream()
                .map(category -> modelMapper.map(category,CategoryDTO.class))
                .toList();

        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setContent(categoryDTOList);
        categoryResponse.setPageNumber(categoryPage.getNumber());
        categoryResponse.setPageSize(categoryPage.getSize());
        categoryResponse.setTotalElements(categoryPage.getTotalElements());
        categoryResponse.setTotalPages(categoryPage.getTotalPages());
        categoryResponse.setLastPage(categoryPage.isLast());

        return categoryResponse;
    }

    @Override
    public CategoryDTO createCategory(CategoryDTO categoryDTO) {

        Category category = modelMapper.map(categoryDTO, Category.class);

        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(optionalCategory.isPresent()){
            throw new APIException("Category with name "+category.getCategoryName()+" already exists!!!");
        }

        category.setCategoryId(null);
        Category savedCategory = categoryRepository.save(category);
        CategoryDTO savedCategoryDTO = modelMapper.map(savedCategory,CategoryDTO.class);
        return savedCategoryDTO;
    }

    @Override
    public CategoryDTO deleteCategory(Long categoryId) {

        Category categoryToDelete = categoryRepository.findById(categoryId)
                .orElseThrow(
                        ()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        categoryRepository.delete(categoryToDelete);
        return  modelMapper.map(categoryToDelete,CategoryDTO.class);
    }

    @Override
    public CategoryDTO updateCategory(CategoryDTO categoryDTO, Long categoryId) {

        Category category = modelMapper.map(categoryDTO, Category.class);

        Category categoryToUpdate = categoryRepository.findById(categoryId)
                .orElseThrow(()-> new ResourceNotFoundException("Category","CategoryId",categoryId));

        Optional<Category> optionalCategory = categoryRepository.findByCategoryName(category.getCategoryName());
        if(optionalCategory.isPresent()){
            throw new APIException("Category with name "+category.getCategoryName()+" already exists!!!");
        }

        category.setCategoryId(categoryId);
        Category updatedCategory = categoryRepository.save(category);
        return modelMapper.map(updatedCategory,CategoryDTO.class);
    }
}
