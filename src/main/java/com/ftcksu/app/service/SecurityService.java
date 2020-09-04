package com.ftcksu.app.service;

import com.ftcksu.app.model.entity.User;
import com.ftcksu.app.model.request.AuthenticationRequest;
import com.ftcksu.app.security.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    private final AuthenticationManager authenticationManager;
    private final UserDetailsService userDetailsService;
    private final UserService userService;
    private final JWTUtil jwt;

    @Autowired
    public SecurityService(AuthenticationManager authenticationManager,
                           UserDetailsService userDetailsService,
                           UserService userService,
                           JWTUtil jwt) {
        this.authenticationManager = authenticationManager;
        this.userDetailsService = userDetailsService;
        this.userService = userService;
        this.jwt = jwt;
    }

    public User getLoggedUser() {
        Object userDetails = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        if (userDetails instanceof UserDetails) {
            return userService.getUserById(Integer.parseInt(((UserDetails) userDetails).getUsername()));
        }

        return null;
    }

    public String login(AuthenticationRequest request) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(userDetails,
                request.getPassword(), userDetails.getAuthorities());

        authenticationManager.authenticate(authenticationToken);

        if (authenticationToken.isAuthenticated()) {
            SecurityContextHolder.getContext().setAuthentication(authenticationToken);
        }

        return jwt.generateToken(userDetails);
    }
}

