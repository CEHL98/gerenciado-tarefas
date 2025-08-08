/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.dto.tarefa;

import com.carlos.todoapi.domain.enums.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author carlos.lacerda
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Requisição para criação de nova tarefa")
public class TarefaRequest {

    @Schema(description = "Descrição da tarefa", example = "Finalizar relatório mensal", required = true)
    @NotBlank(message = "A descrição é obrigatória.")
    private String descricao;

    @Schema(description = "Prioridade da tarefa", example = "ALTA", required = true)
    @NotNull(message = "A prioridade é obrigatória.")
    private Prioridade prioridade;
}