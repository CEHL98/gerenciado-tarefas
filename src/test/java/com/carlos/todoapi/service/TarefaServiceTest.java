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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TarefaServiceTest {

    @Mock
    private TarefaRepository tarefaRepository;

    @Mock
    private TarefaFactory tarefaFactory;

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TarefaService tarefaService;

    @Test
    void criarTarefa_ComDadosValidos_DeveRetornarTarefa() {
        TarefaRequest request = new TarefaRequest();
        Tarefa tarefaMock = new Tarefa();

        when(tarefaFactory.criarNovaTarefa(request, authentication)).thenReturn(tarefaMock);
        when(tarefaRepository.salvar(tarefaMock)).thenReturn(tarefaMock);

        Tarefa resultado = tarefaService.criarTarefa(request, authentication);

        assertNotNull(resultado);
        verify(tarefaRepository).salvar(tarefaMock);
    }

    @Test
    void excluirTarefa_ComPermissao_DeveExcluir() {
        Long idTarefa = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuarioId(1L);

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorId(idTarefa)).thenReturn(Optional.of(tarefa));

        tarefaService.excluirTarefa(idTarefa, authentication);

        verify(tarefaRepository).deletar(idTarefa);
    }

    @Test
    void excluirTarefa_QuandoNaoEncontrada_DeveLancarExcecao() {
        Long idTarefa = 1L;
        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(new Usuario()));
        when(tarefaRepository.buscarPorId(idTarefa)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaService.excluirTarefa(idTarefa, authentication);
        });
        assertEquals("Tarefa não encontrada.", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void excluirTarefa_QuandoUsuarioNaoAutorizado_DeveLancarExcecao() {
        Long idTarefa = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuarioId(2L); // ID diferente

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorId(idTarefa)).thenReturn(Optional.of(tarefa));

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaService.excluirTarefa(idTarefa, authentication);
        });
        assertEquals("Você não pode excluir tarefas de outro usuário.", exception.getMessage());
        assertEquals(HttpStatus.FORBIDDEN, exception.getStatus());
    }

    @Test
    void listarTarefasPendentes_SemPrioridade_DeveRetornarLista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        List<Tarefa> tarefas = List.of(new Tarefa());
        List<TarefaResponse> responses = List.of(new TarefaResponse());

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, 1L)).thenReturn(tarefas);
        when(tarefaFactory.criarLista(tarefas)).thenReturn(responses);

        List<TarefaResponse> resultado = tarefaService.listarTarefasPendentes(authentication, null);

        assertFalse(resultado.isEmpty());
    }

    @Test
    void listarTarefasPendentes_ComPrioridade_DeveRetornarLista() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        List<Tarefa> tarefas = List.of(new Tarefa());
        List<TarefaResponse> responses = List.of(new TarefaResponse());

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(Status.TO_DO, Prioridade.ALTA, 1L)).thenReturn(tarefas);
        when(tarefaFactory.criarLista(tarefas)).thenReturn(responses);

        List<TarefaResponse> resultado = tarefaService.listarTarefasPendentes(authentication, Prioridade.ALTA);

        assertFalse(resultado.isEmpty());
    }

    @Test
    void listarTarefasPendentes_QuandoNenhumaTarefa_DeveLancarExcecao() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, 1L)).thenReturn(List.of());

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaService.listarTarefasPendentes(authentication, null);
        });
        assertEquals("Nenhuma tarefa encontrada para os filtros informados", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }

    @Test
    void concluirTarefa_ComPermissao_DeveAtualizarStatus() {
        Long idTarefa = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuarioId(1L);
        tarefa.setStatus(Status.TO_DO);

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorIdEUsuarioId(idTarefa, 1L)).thenReturn(Optional.of(tarefa));

        tarefaService.concluirTarefa(idTarefa, authentication);

        verify(tarefaRepository).atualizarStatus(idTarefa, Status.DONE);
    }

    @Test
    void concluirTarefa_QuandoJaConcluida_DeveLancarExcecao() {
        Long idTarefa = 1L;
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuarioId(1L);
        tarefa.setStatus(Status.DONE);

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorIdEUsuarioId(idTarefa, 1L)).thenReturn(Optional.of(tarefa));

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaService.concluirTarefa(idTarefa, authentication);
        });
        assertEquals("Tarefa já está concluída", exception.getMessage());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatus());
    }

    @Test
    void atualizarTarefa_ComPermissao_DeveAtualizar() {
        Long idTarefa = 1L;
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("Nova descrição");
        request.setPrioridade(Prioridade.ALTA);

        Usuario usuario = new Usuario();
        usuario.setId(1L);
        Tarefa tarefa = new Tarefa();
        tarefa.setUsuarioId(1L);

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(usuario));
        when(tarefaRepository.buscarPorId(idTarefa)).thenReturn(Optional.of(tarefa));

        Tarefa resultado = tarefaService.atualizarTarefa(idTarefa, request, authentication);

        assertEquals("Nova descrição", resultado.getDescricao());
        assertEquals(Prioridade.ALTA, resultado.getPrioridade());
        verify(tarefaRepository).atualizarTarefa(tarefa);
    }

    @Test
    void atualizarTarefa_QuandoNaoEncontrada_DeveLancarExcecao() {
        Long idTarefa = 1L;
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();

        when(authentication.getName()).thenReturn("usuario@email.com");
        when(usuarioRepository.findByEmail("usuario@email.com")).thenReturn(Optional.of(new Usuario()));
        when(tarefaRepository.buscarPorId(idTarefa)).thenReturn(Optional.empty());

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaService.atualizarTarefa(idTarefa, request, authentication);
        });
        assertEquals("Tarefa não encontrada", exception.getMessage());
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatus());
    }
}