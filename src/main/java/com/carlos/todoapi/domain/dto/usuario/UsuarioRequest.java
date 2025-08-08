/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.dto.usuario;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
@Schema(description = "Requisição para cadastro de novo usuário.")
public class UsuarioRequest {

    @Schema(description = "Nome completo do usuário", example = "João da Silva",required = true)
    @NotBlank(message = "O nome é obrigatório.")
    private String nome;

    @Schema(description = "E-mail válido do usuário",example = "joao.silva@email.com",required = true)
    @NotBlank(message = "O e-mail é obrigatório.")
    @Email(message = "E-mail inválido.")
    private String email;

    @Schema(description = "Senha segura contendo maiúsculas, minúsculas, números e caractere especial",example = "Senha@123",required = true)
    @NotBlank(message = "A senha é obrigatória.")
    @Pattern( regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%&*]).+$",message = "Senha não corresponde aos requisitos mínimos de segurança. A senha deve conter letras maiúsculas, minúsculas, números e pelo menos 1 caractere especial EX:!@#$%&*")
    private String senha;
}
