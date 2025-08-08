/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.controller;

import com.carlos.todoapi.domain.dto.usuario.LoginRequest;
import com.carlos.todoapi.domain.dto.usuario.UsuarioRequest;
import com.carlos.todoapi.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 *
 * @author carlos.lacerda
 */
@RestController
@RequestMapping("/auth")
@Tag(name = "Autenticação", description = "Endpoints de autenticação e registro de usuários")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
            summary = "Registrar novo usuário",
            description = "Cria um novo usuário com nome, e-mail e senha. A senha deve seguir a política de segurança.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Usuário criado com sucesso. Token JWT retornado.",
                        content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = "400", description = "Requisição inválida ou e-mail já cadastrado",
                        content = @Content(schema = @Schema(example = "Erro na requisição")))
            }
    )
    @PostMapping("/registrar")
    public ResponseEntity<String> registrar(@RequestBody @Valid UsuarioRequest request) {
        String token = authService.registrar(request);
        return ResponseEntity.ok(token);
    }

    @Operation(
            summary = "Autenticar usuário",
            description = "Realiza login com e-mail e senha e retorna um token JWT caso seja bem-sucedido.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Autenticação realizada com sucesso. Token JWT retornado.",
                        content = @Content(schema = @Schema(implementation = String.class))),
                @ApiResponse(responseCode = "401", description = "Credenciais inválidas",
                        content = @Content(schema = @Schema(example = "Usuário ou senha inválidos")))
            }
    )
    @PostMapping("/login")
    public ResponseEntity<String> login(@RequestBody @Valid LoginRequest request) {
        String token = authService.autenticar(request);
        return ResponseEntity.ok(token);
    }
}
