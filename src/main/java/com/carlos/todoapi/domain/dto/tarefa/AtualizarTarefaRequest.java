package com.carlos.todoapi.domain.dto.tarefa;

import com.carlos.todoapi.domain.enums.Prioridade;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Requisição para atualização de uma tarefa")
@Data
public class AtualizarTarefaRequest {

    @Schema(description = "Nova descrição da tarefa", example = "Atualizar documentação")
    @NotBlank(message = "A descrição não pode estar vazia")
    @Size(max = 255, message = "A descrição deve ter no máximo 255 caracteres")
    private String descricao;

    @Schema(description = "Nova prioridade da tarefa", example = "ALTA")
    @NotNull(message = "A prioridade é obrigatória")
    private Prioridade prioridade;
}
