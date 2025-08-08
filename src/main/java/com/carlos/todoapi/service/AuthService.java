/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.service;

import com.carlos.todoapi.config.JwtService;
import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.domain.dto.usuario.LoginRequest;
import com.carlos.todoapi.domain.dto.usuario.UsuarioRequest;
import com.carlos.todoapi.exception.ApiException;
import com.carlos.todoapi.factory.UsuarioFactory;
import com.carlos.todoapi.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 *
 * @author carlos.lacerda
 */
@Service
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UsuarioFactory usuarioFactory;

    public AuthService(UsuarioRepository usuarioRepository, JwtService jwtService, AuthenticationManager authenticationManager, UsuarioFactory usuarioFactory) {
        this.usuarioRepository = usuarioRepository;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.usuarioFactory = usuarioFactory;
    }

    public String registrar(UsuarioRequest request) {
        if (usuarioRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new ApiException("Email já cadastrado", HttpStatus.BAD_REQUEST);
        }

        Usuario usuario = usuarioFactory.criarNovoUsuario(request);
        usuarioRepository.save(usuario);

        return jwtService.generateToken(usuario);
    }

    public String autenticar(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getSenha()
                )
        );

        UserDetails userDetails = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ApiException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        return jwtService.generateToken(userDetails);

    }

}
