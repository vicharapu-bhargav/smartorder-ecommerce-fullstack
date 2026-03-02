package com.ecommerce.sb_ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long productId;

    @NotBlank
    @Size(min=4, message = "Product Name should be a minimum of 4 characters")
    private String productName;

    private String image;

    @NotBlank
    @Size(min=10, message = "Product Description should be a minimum of 10 characters")
    private String description;

    @Min(value=0,message = "Quantity should not be less than zero!!!")
    private Integer quantity;

    @Min(value=0,message = "Price should not be less than zero!!!")
    private Double price;

    @Min(value=0,message = "Discount should not be less than zero !!!")
    @Max(value=100,message = "Discount should not be more than 100 !!!")
    private Double discount;

    @Min(value=0,message = "Quantity should not be less than zero!!!")
    private Double specialPrice;

    @ManyToOne
    @JoinColumn(name="category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name="seller_id")
    private User user;
}
