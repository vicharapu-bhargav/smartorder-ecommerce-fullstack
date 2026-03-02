package com.ecommerce.sb_ecom.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;

    @NotBlank
    @Size(min = 5, message = "Street Name should be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building Name should be at least 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 3, message = "City Name should be at least 3 characters")
    private String city;

    @NotBlank
    @Size(min = 2, message = "State Name should be at least 2 characters")
    private String state;

    @NotBlank
    @Size(min = 2, message = "country Name should be at least 2 characters")
    private String country;

    @NotBlank
    @Size(min = 6, message = "Pin code should be at least 6 characters")
    private String pincode;


    @ToString.Exclude
    @ManyToMany(mappedBy = "")
    private List<User> users = new ArrayList<>();

    public Address(String street, String buildingName, String city, String state, String country, String pincode) {
        this.street = street;
        this.buildingName = buildingName;
        this.city = city;
        this.state = state;
        this.country = country;
        this.pincode = pincode;
    }
}
