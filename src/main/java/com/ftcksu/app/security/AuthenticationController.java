package com.ftcksu.app.security;

import com.ftcksu.app.model.request.AuthenticationRequest;
import com.ftcksu.app.model.response.AuthenticationResponse;
import com.ftcksu.app.service.SecurityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@RestController
public class AuthenticationController {
    private final SecurityService securityService;

    @Autowired
    public AuthenticationController(SecurityService securityService) {
        this.securityService = securityService;
    }

    @PostMapping(value = "/login")
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) {
        String token = securityService.login(authenticationRequest);
        return ResponseEntity.ok(new AuthenticationResponse(token));
    }

}
