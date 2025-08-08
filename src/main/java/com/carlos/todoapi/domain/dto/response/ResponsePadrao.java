package com.carlos.todoapi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import io.swagger.v3.oas.annotations.media.Schema;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Modelo padrão de resposta para sucesso ou erro")
public class ResponsePadrao {

    @Schema(description = "Título da resposta", example = "Sucesso")
    private String titulo;

    @Schema(description = "Mensagem detalhada da resposta", example = "Operação finalizada com sucesso.")
    private String mensagem;
}
