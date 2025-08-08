/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Email;

/**
 *
 * @author carlos.lacerda
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Requisição para autenticação de usuário")
public class LoginRequest {

    @Schema(description = "E-mail do usuário", example = "joao@email.com", required = true)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;

    @Schema(description = "Senha do usuário", example = "Senha@123", required = true)
    @NotBlank(message = "A senha é obrigatória.")
    private String senha;
}