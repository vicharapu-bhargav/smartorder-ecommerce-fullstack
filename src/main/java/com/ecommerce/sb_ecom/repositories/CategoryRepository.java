package com.ecommerce.sb_ecom.repositories;

import com.ecommerce.sb_ecom.model.Category;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category,Long> {
    Optional<Category> findByCategoryName(@NotBlank String categoryName);
}
