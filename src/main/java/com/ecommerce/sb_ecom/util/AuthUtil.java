package com.ecommerce.sb_ecom.util;

import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

@Component
public class AuthUtil {

    @Autowired
    UserRepository userRepository;

    public User loggedInUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUserName(authentication.getName())
                .orElseThrow(()->new UsernameNotFoundException("Username not found"));

        return user;
    }


    public String loggedInEmail(){
        User user = loggedInUser();
        return user.getEmail();
    }


    public Long loggedInUserId(){
        User user = loggedInUser();
        return user.getUserId();
    }

}
