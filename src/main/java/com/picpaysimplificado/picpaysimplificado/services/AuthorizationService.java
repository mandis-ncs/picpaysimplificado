package com.picpaysimplificado.picpaysimplificado.services;

import com.picpaysimplificado.picpaysimplificado.domain.authorization.AuthorizationResponse;
import com.picpaysimplificado.picpaysimplificado.domain.user.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AuthorizationService {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${app.authorizationApi}")
    private String authApiUrl;

    public boolean authorizeTransaction(User sender, BigDecimal value){
        ResponseEntity<AuthorizationResponse> authorizationResponse =
                restTemplate.getForEntity(this.authApiUrl, AuthorizationResponse.class);

        if (authorizationResponse.getStatusCode() == HttpStatus.OK) {
            AuthorizationResponse body = authorizationResponse.getBody();

            if (body != null && "success".equalsIgnoreCase(body.status())) {
                return Boolean.TRUE.equals(body.data().authorization());
            }
        } return false;
    }

}
