/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.controller;

import com.carlos.todoapi.domain.bean.Tarefa;
import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.domain.dto.response.ResponsePadrao;
import com.carlos.todoapi.domain.dto.tarefa.AtualizarTarefaRequest;
import com.carlos.todoapi.domain.dto.tarefa.TarefaRequest;
import com.carlos.todoapi.domain.dto.tarefa.TarefaResponse;
import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.service.TarefaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 *
 * @author carlos.lacerda
 */
@RestController
@RequestMapping("/tarefas")
@Tag(name = "Tarefas", description = "Endpoints para gerenciamento de tarefas")
public class TarefaController {

    private final TarefaService tarefaService;

    public TarefaController(TarefaService tarefaService) {
        this.tarefaService = tarefaService;
    }

    @Operation(
            summary = "Cria uma nova tarefa",
            description = "Cria uma nova tarefa com descrição e prioridade. O usuário é obtido via token JWT.",
            responses = {
                @ApiResponse(responseCode = "200", description = "Tarefa criada com sucesso",
                        content = @Content(schema = @Schema(implementation = Tarefa.class))),
                @ApiResponse(responseCode = "400", description = "Dados inválidos",
                        content = @Content(schema = @Schema(example = "{\"message\": \"Descrição é obrigatória\"}")))
            }
    )
    @PostMapping
    public ResponseEntity<Tarefa> criarTarefa(@RequestBody @Valid TarefaRequest request, Authentication authentication) {
        Tarefa tarefaCriada = tarefaService.criarTarefa(request, authentication);
        return ResponseEntity.ok(tarefaCriada);
    }

    @DeleteMapping("/{id}")
    @Operation(
            summary = "Excluir tarefa",
            description = "Exclui uma tarefa pelo seu ID, somente se ela pertencer ao usuário autenticado.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tarefa excluída com sucesso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponsePadrao.class))),
                    @ApiResponse(responseCode = "403", description = "Usuário não autorizado a excluir essa tarefa",
                            content = @Content(schema = @Schema(example = "Você não pode excluir tarefas de outro usuário."))),
                    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada",
                            content = @Content(schema = @Schema(example = "Tarefa não encontrada.")))
            }
    )
    public ResponseEntity<ResponsePadrao> excluirTarefa(@PathVariable Long id, Authentication authentication) {
        tarefaService.excluirTarefa(id, authentication);
        ResponsePadrao resposta = new ResponsePadrao("Sucesso", "Tarefa excluída com sucesso.");
        return ResponseEntity.ok(resposta);
    }

    @Operation(
            summary = "Listar tarefas pendentes (TO_DO)",
            responses = {
                @ApiResponse(responseCode = "200", description = "Lista de tarefas retornada com sucesso",
                        content = @Content(array = @ArraySchema(schema = @Schema(implementation = TarefaResponse.class)))
                ),
                @ApiResponse(responseCode = "404", description = "Nenhuma tarefa encontrada para os filtros informados"
                )
            }
    )
    @GetMapping("/pendentes")
    public ResponseEntity<List<TarefaResponse>> listarTarefasPendentes(
            @Parameter(description = "Prioridade para filtrar (opcional)")
            @RequestParam(required = false) Prioridade prioridade,
            Authentication authentication) {

        List<TarefaResponse> tarefas = tarefaService.listarTarefasPendentes(authentication, prioridade);
        return ResponseEntity.ok(tarefas);
    }


    @PatchMapping("/{id}/concluir")
    @Operation(
            summary = "Marcar tarefa como concluída",
            description = "Marca a tarefa como concluída. O usuário só pode concluir suas próprias tarefas.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tarefa marcada como concluída com sucesso",
                            content = @Content(mediaType = "application/json",
                                    schema = @Schema(implementation = ResponsePadrao.class))),
                    @ApiResponse(responseCode = "403", description = "Usuário não autorizado a concluir essa tarefa"),
                    @ApiResponse(responseCode = "404", description = "Tarefa ou usuário não encontrado")
            }
    )
    public ResponseEntity<ResponsePadrao> concluirTarefa(@PathVariable Long id, Authentication authentication) {
        tarefaService.concluirTarefa(id, authentication);
        ResponsePadrao resposta = new ResponsePadrao("Sucesso", "Tarefa marcada como concluída com sucesso");
        return ResponseEntity.ok(resposta);
    }


    @Operation(
            summary = "Atualizar uma tarefa",
            description = "Atualiza a descrição e prioridade de uma tarefa específica. Apenas o dono da tarefa pode modificá-la.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso.", content = @Content(schema = @Schema(implementation = Tarefa.class))),
                    @ApiResponse(responseCode = "404", description = "Tarefa não encontrada ou usuário não autorizado.", content = @Content(schema = @Schema(example = "Tarefa não encontrada ou acesso negado."))),
                    @ApiResponse(responseCode = "400", description = "Dados de entrada inválidos.", content = @Content(schema = @Schema(example = "A prioridade é obrigatória.")))
            }
    )
    @PutMapping("/{id}")
    public ResponseEntity<Tarefa> atualizarTarefa(
            @PathVariable Long id,
            @Valid @RequestBody AtualizarTarefaRequest request,
            Authentication authentication) {

        Tarefa tarefaAtualizada = tarefaService.atualizarTarefa(id, request, authentication);
        return ResponseEntity.ok(tarefaAtualizada);
    }

}
