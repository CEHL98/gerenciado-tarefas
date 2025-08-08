/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.service;

import com.carlos.todoapi.domain.bean.Tarefa;
import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.domain.dto.tarefa.AtualizarTarefaRequest;
import com.carlos.todoapi.domain.dto.tarefa.TarefaRequest;
import com.carlos.todoapi.domain.dto.tarefa.TarefaResponse;
import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.domain.enums.Status;
import com.carlos.todoapi.exception.ApiException;
import com.carlos.todoapi.factory.TarefaFactory;
import com.carlos.todoapi.repository.TarefaRepository;
import com.carlos.todoapi.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 *
 * @author carlos.lacerda
 */
@Service
public class TarefaService {

    private final TarefaRepository tarefaRepository;
    private final TarefaFactory tarefaFactory;
    private final UsuarioRepository usuarioRepository;

    public TarefaService(TarefaRepository tarefaRepository, TarefaFactory tarefaFactory, UsuarioRepository usuarioRepository) {
        this.tarefaRepository = tarefaRepository;
        this.tarefaFactory = tarefaFactory;
        this.usuarioRepository = usuarioRepository;
    }

    public Tarefa criarTarefa(TarefaRequest request, Authentication authentication) {
        Tarefa novaTarefa = tarefaFactory.criarNovaTarefa(request, authentication);
        return tarefaRepository.salvar(novaTarefa);
    }

    public void excluirTarefa(Long idTarefa, Authentication authentication) {

        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ApiException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        Optional<Tarefa> tarefaOpt = tarefaRepository.buscarPorId(idTarefa);
        if (tarefaOpt.isEmpty()) {
            throw new ApiException("Tarefa não encontrada.", HttpStatus.NOT_FOUND);
        }

        Tarefa tarefa = tarefaOpt.get();

        if (!tarefa.getUsuarioId().equals(usuario.getId())) {
            throw new ApiException("Você não pode excluir tarefas de outro usuário.", HttpStatus.FORBIDDEN);
        }

        tarefaRepository.deletar(idTarefa);
    }

    public List<TarefaResponse> listarTarefasPendentes(Authentication authentication, Prioridade prioridade) {
        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ApiException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        List<Tarefa> tarefas;

        if (prioridade != null) {
            tarefas = tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(Status.TO_DO, prioridade, usuario.getId());
        } else {
            tarefas = tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, usuario.getId());
        }

        if (tarefas.isEmpty()) {
            throw new ApiException("Nenhuma tarefa encontrada para os filtros informados", HttpStatus.NOT_FOUND);
        }

        return tarefaFactory.criarLista(tarefas);
    }

    public void concluirTarefa(Long id, Authentication authentication) {
        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ApiException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        Tarefa tarefa = tarefaRepository.buscarPorIdEUsuarioId(id, usuario.getId())
                .orElseThrow(() -> new ApiException("Tarefa não encontrada ou não pertence ao usuário", HttpStatus.FORBIDDEN));

        if (tarefa.getStatus() == Status.DONE) {
            throw new ApiException("Tarefa já está concluída", HttpStatus.BAD_REQUEST);
        }

        tarefaRepository.atualizarStatus(id, Status.DONE);
    }

    public Tarefa atualizarTarefa(Long tarefaId, AtualizarTarefaRequest request, Authentication authentication) {
        String emailUsuario = authentication.getName();
        Usuario usuario = usuarioRepository.findByEmail(emailUsuario)
                .orElseThrow(() -> new ApiException("Usuário não encontrado", HttpStatus.NOT_FOUND));

        Tarefa tarefa = tarefaRepository.buscarPorId(tarefaId)
                .orElseThrow(() -> new ApiException("Tarefa não encontrada", HttpStatus.NOT_FOUND));

        if (!tarefa.getUsuarioId().equals(usuario.getId())) {
            throw new ApiException("Usuário não autorizado a alterar esta tarefa", HttpStatus.FORBIDDEN);
        }


        tarefa.setDescricao(request.getDescricao());
        tarefa.setPrioridade(request.getPrioridade());

        tarefaRepository.atualizarTarefa(tarefa);

        return tarefa;
    }
}
