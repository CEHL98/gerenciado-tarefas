/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.dto.tarefa;

import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.domain.enums.Status;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author carlos.lacerda
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Representação de uma tarefa na resposta da API")
public class TarefaResponse {

    @Schema(description = "ID da tarefa", example = "1")
    private Long id;

    @Schema(description = "Descrição da tarefa", example = "Implementar endpoint GET /tarefas")
    private String descricao;

    @Schema(description = "Prioridade da tarefa", example = "ALTA")
    private Prioridade prioridade;

    @Schema(
            description = "Status atual da tarefa no fluxo Kanban",
            example = "TO_DO",
            allowableValues = {"TO_DO", "DOING", "DONE"}
    )
    private Status status;
}
