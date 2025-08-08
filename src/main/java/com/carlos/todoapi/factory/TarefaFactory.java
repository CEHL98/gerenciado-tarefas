/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.factory;

import com.carlos.todoapi.domain.bean.Tarefa;
import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.domain.dto.tarefa.TarefaRequest;
import com.carlos.todoapi.domain.dto.tarefa.TarefaResponse;
import com.carlos.todoapi.domain.enums.Status;
import com.carlos.todoapi.exception.ApiException;
import com.carlos.todoapi.repository.UsuarioRepository;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 *
 * @author carlos.lacerda
 */
@Component
public class TarefaFactory {

    private final UsuarioRepository usuarioRepository;

    public TarefaFactory(UsuarioRepository usuarioRepository) {
        this.usuarioRepository = usuarioRepository;
    }

    public Tarefa criarNovaTarefa(TarefaRequest request, Authentication authentication) {
        String emailUsuario = authentication.getName();

        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ApiException("Usuário não encontrado.", HttpStatus.NOT_FOUND));

        Tarefa tarefa = new Tarefa();
        tarefa.setDescricao(request.getDescricao());
        tarefa.setPrioridade(request.getPrioridade());
        tarefa.setUsuarioId(usuario.getId());
        tarefa.setStatus(Status.TO_DO);

        return tarefa;
    }

    public TarefaResponse criar(Tarefa tarefa) {
        TarefaResponse response = new TarefaResponse();
        response.setId(tarefa.getId());
        response.setDescricao(tarefa.getDescricao());
        response.setPrioridade(tarefa.getPrioridade());
        response.setStatus(tarefa.getStatus());
        return response;
    }

    public List<TarefaResponse> criarLista(List<Tarefa> tarefas) {
        return tarefas.stream()
                .map(this::criar)
                .collect(Collectors.toList());
    }

}
