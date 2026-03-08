package com.ecommerce.sb_ecom.security.services;

import com.ecommerce.sb_ecom.model.User;
import com.ecommerce.sb_ecom.repositories.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUserName(username).orElseThrow(
                ()-> new UsernameNotFoundException("User not found with Username: "+username));

        return UserDetailsImpl.build(user);
    }
}
