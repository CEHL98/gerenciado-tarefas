/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.config;

import com.carlos.todoapi.exception.ApiException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

/**
 *
 * @author Particular
 */
@Component
public class CustomAuthEntryPoint implements AuthenticationEntryPoint {
    
    @Override
    public void commence(HttpServletRequest request, 
                        HttpServletResponse response,
                        AuthenticationException authException) throws IOException {

        ApiException apiException = new ApiException(
                "Acesso negado. Verifique suas credenciais e tente novamente.",
                HttpStatus.UNAUTHORIZED
        );

        response.setContentType("application/json");
        response.setStatus(apiException.getStatus().value());
        response.getWriter().write(
            String.format("{\"status\": %d, \"message\": \"%s\"}",
                apiException.getStatus().value(),
                apiException.getMessage())
        );
    }
}
