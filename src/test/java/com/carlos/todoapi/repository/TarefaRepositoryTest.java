package com.carlos.todoapi.repository;

import com.carlos.todoapi.domain.bean.Tarefa;
import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.domain.enums.Status;
import com.carlos.todoapi.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
class TarefaRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private TarefaRepository tarefaRepository;

    @BeforeEach
    void setUp() {
        tarefaRepository = new TarefaRepository(jdbcTemplate);

        jdbcTemplate.execute("DROP TABLE IF EXISTS tarefa");
        jdbcTemplate.execute("CREATE TABLE tarefa (" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "descricao VARCHAR(255), " +
                "prioridade VARCHAR(50), " +
                "status VARCHAR(50), " +
                "usuario_id BIGINT)");
    }

    @Test
    void salvar_QuandoDadosValidos_DeveRetornarTarefaComId() {
        Tarefa novaTarefa = new Tarefa();
        novaTarefa.setDescricao("Estudar JUnit 5");
        novaTarefa.setPrioridade(Prioridade.ALTA);
        novaTarefa.setStatus(Status.TO_DO);
        novaTarefa.setUsuarioId(1L);

        Tarefa tarefaSalva = tarefaRepository.salvar(novaTarefa);

        assertNotNull(tarefaSalva.getId(), "Deveria ter gerado um ID");
        assertEquals("Estudar JUnit 5", tarefaSalva.getDescricao());
        assertEquals(Prioridade.ALTA, tarefaSalva.getPrioridade());
        assertEquals(Status.TO_DO, tarefaSalva.getStatus());
        assertEquals(1L, tarefaSalva.getUsuarioId());

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tarefa WHERE id = ?",
                Integer.class,
                tarefaSalva.getId()
        );
        assertEquals(1, count, "Deveria existir a tarefa no banco");
    }

    @Test
    void salvar_QuandoOcorreErro_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE tarefa");

        Tarefa tarefaInvalida = new Tarefa();
        tarefaInvalida.setDescricao("Tarefa inválida");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaRepository.salvar(tarefaInvalida);
        });

        assertEquals("Erro ao salvar a tarefa no banco de dados.", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void buscarPorId_QuandoTarefaExiste_DeveRetornarTarefa() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Revisar código", "MEDIA", "DOING", 2L
        );

        Long idInserido = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = ?",
                Long.class,
                "Revisar código"
        );

        Optional<Tarefa> resultado = tarefaRepository.buscarPorId(idInserido);

        assertTrue(resultado.isPresent(), "Deveria encontrar a tarefa");
        assertEquals("Revisar código", resultado.get().getDescricao());
        assertEquals(Prioridade.MEDIA, resultado.get().getPrioridade());
        assertEquals(Status.DOING, resultado.get().getStatus());
        assertEquals(2L, resultado.get().getUsuarioId());
    }

    @Test
    void buscarPorId_QuandoTarefaNaoExiste_DeveRetornarOptionalVazio() {
        Optional<Tarefa> resultado = tarefaRepository.buscarPorId(999L);

        assertFalse(resultado.isPresent(), "Não deveria encontrar tarefa");
    }

    @Test
    void buscarPorIdEUsuarioId_QuandoTarefaExisteParaUsuario_DeveRetornarTarefa() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa User 1", "MEDIA", "TO_DO", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa User 2", "ALTA", "DOING", 2L
        );

        Long idTarefaUser1 = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE usuario_id = 1", Long.class
        );

        Optional<Tarefa> resultado = tarefaRepository.buscarPorIdEUsuarioId(idTarefaUser1, 1L);

        assertTrue(resultado.isPresent(), "Deveria encontrar a tarefa do usuário 1");
        assertEquals("Tarefa User 1", resultado.get().getDescricao());
        assertEquals(1L, resultado.get().getUsuarioId());
    }

    @Test
    void buscarPorIdEUsuarioId_QuandoTarefaNaoPertenceAoUsuario_DeveRetornarVazio() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa User 2", "ALTA", "DOING", 2L
        );

        Long idTarefaUser2 = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE usuario_id = 2", Long.class
        );

        Optional<Tarefa> resultado = tarefaRepository.buscarPorIdEUsuarioId(idTarefaUser2, 1L);

        assertFalse(resultado.isPresent(), "Não deveria encontrar tarefa de outro usuário");
    }

    @Test
    void buscarPorIdEUsuarioId_QuandoOcorreErro_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE tarefa");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaRepository.buscarPorIdEUsuarioId(1L, 1L);
        });

        assertEquals("Erro ao buscar a tarefa por ID e usuário.", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void deletar_QuandoTarefaExiste_DeveRemoverDoBanco() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa para deletar", "BAIXA", "TO_DO", 1L
        );

        Long id = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = ?",
                Long.class,
                "Tarefa para deletar"
        );

        tarefaRepository.deletar(id);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tarefa WHERE id = ?",
                Integer.class,
                id
        );
        assertEquals(0, count, "A tarefa deveria ter sido removida");
    }

    @Test
    void deletar_QuandoTarefaNaoExiste_NaoDeveLancarExcecao() {
        assertDoesNotThrow(() -> {
            tarefaRepository.deletar(999L);
        });

    }

    @Test
    void deletar_DeveRemoverApenasTarefaEspecifica() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 1", "MEDIA", "TO_DO", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 2", "ALTA", "DOIN", 1L
        );

        Long idTarefa1 = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = 'Tarefa 1'", Long.class
        );

        tarefaRepository.deletar(idTarefa1);

        Integer countTarefa1 = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tarefa WHERE id = ?",
                Integer.class,
                idTarefa1
        );
        assertEquals(0, countTarefa1, "Tarefa 1 deveria ter sido removida");

        Integer countTotal = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tarefa",
                Integer.class
        );
        assertEquals(1, countTotal, "Deveria restar apenas uma tarefa");
    }

    private void inserirDadosTeste() {
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 1 User 1", "ALTA", "TO_DO", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 2 User 1", "MEDIA", "DOING", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 3 User 1", "BAIXA", "DONE", 1L
        );
        jdbcTemplate.update(
                "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)",
                "Tarefa 1 User 2", "ALTA", "TO_DO", 2L
        );
    }

    @Test
    void buscarPorStatusEUsuarioId_QuandoStatusPendente_DeveRetornarTarefasCorretas() {
        inserirDadosTeste();

        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, 1L);

        assertEquals(1, resultado.size());
        assertEquals("Tarefa 1 User 1", resultado.get(0).getDescricao());
    }

    @Test
    void buscarPorStatusEUsuarioId_QuandoStatusConcluida_DeveRetornarTarefasCorretas() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEUsuarioId(Status.DONE, 1L);

        assertEquals(1, resultado.size(), "Deveria retornar 1 tarefa CONCLUIDA para o usuário 1");
        assertEquals("Tarefa 3 User 1", resultado.get(0).getDescricao());
    }

    @Test
    void buscarPorStatusEUsuarioId_QuandoNaoExistemTarefas_DeveRetornarListaVazia() {
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEUsuarioId(Status.DONE, 1L);

        assertTrue(resultado.isEmpty(), "Deveria retornar lista vazia para status CANCELADA");
    }

    @Test
    void buscarPorStatusEUsuarioId_DeveFiltrarPorUsuarioCorreto() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, 2L);

        assertEquals(1, resultado.size(), "Deveria retornar 1 tarefa PENDENTE para o usuário 2");
        assertEquals(2L, resultado.get(0).getUsuarioId(), "Deveria ser tarefa do usuário 2");
    }

    @Test
    void buscarPorStatusEUsuarioId_QuandoOcorreErro_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE tarefa");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaRepository.buscarPorStatusEUsuarioId(Status.TO_DO, 1L);
        });

        assertEquals("Erro ao buscar tarefas por status e usuário.", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void buscarPorStatusEPrioridadeEUsuarioId_QuandoStatusPendenteEPrioridadeAlta_DeveRetornarTarefasCorretas() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                Status.TO_DO,
                Prioridade.ALTA,
                1L
        );

        assertEquals(1, resultado.size(), "Deveria retornar 1 tarefa");
        assertEquals("Tarefa 1 User 1", resultado.get(0).getDescricao());
        assertEquals(Prioridade.ALTA, resultado.get(0).getPrioridade());
        assertEquals(Status.TO_DO, resultado.get(0).getStatus());
        assertEquals(1L, resultado.get(0).getUsuarioId());
    }

    @Test
    void buscarPorStatusEPrioridadeEUsuarioId_QuandoStatusEmAndamentoEPrioridadeMedia_DeveRetornarTarefasCorretas() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                Status.DOING,
                Prioridade.MEDIA,
                1L
        );

        assertEquals(1, resultado.size());
        assertEquals("Tarefa 2 User 1", resultado.get(0).getDescricao());
    }

    @Test
    void buscarPorStatusEPrioridadeEUsuarioId_QuandoNaoExistemTarefas_DeveRetornarListaVazia() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                Status.DONE,
                Prioridade.ALTA,
                1L
        );

        assertTrue(resultado.isEmpty(), "Deveria retornar lista vazia");
    }



    @Test
    void buscarPorStatusEPrioridadeEUsuarioId_DeveFiltrarPorUsuarioCorreto() {
        inserirDadosTeste();
        List<Tarefa> resultado = tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                Status.TO_DO,
                Prioridade.ALTA,
                2L
        );

        assertEquals(1, resultado.size());
        assertEquals(2L, resultado.get(0).getUsuarioId(), "Deveria ser tarefa do usuário 2");
    }

    @Test
    void buscarPorStatusEPrioridadeEUsuarioId_QuandoOcorreErro_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE tarefa");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                    Status.TO_DO,
                    Prioridade.ALTA,
                    1L
            );
        });

        assertEquals("Erro ao buscar tarefas por status, prioridade e usuário.", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void buscarPorStatusEUsuarioId_DeveFuncionarParaTodosStatus(Status status) {
        inserirDadosTeste();
        assertDoesNotThrow(() -> {
            tarefaRepository.buscarPorStatusEUsuarioId(status, 1L);
        });
    }

    @ParameterizedTest
    @EnumSource(Prioridade.class)
    void buscarPorStatusEPrioridadeEUsuarioId_DeveFuncionarParaTodasPrioridades(Prioridade prioridade) {
        inserirDadosTeste();
        assertDoesNotThrow(() -> {
            tarefaRepository.buscarPorStatusEPrioridadeEUsuarioId(
                    Status.TO_DO,
                    prioridade,
                    1L
            );
        });
    }

    @Test
    void atualizarStatus_QuandoTarefaExiste_DeveAtualizarStatus() {
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = 'Tarefa 1 User 1'", Long.class);

        tarefaRepository.atualizarStatus(idTarefa, Status.DONE);

        String statusAtualizado = jdbcTemplate.queryForObject(
                "SELECT status FROM tarefa WHERE id = ?",
                String.class,
                idTarefa);

        assertEquals("DONE", statusAtualizado);
    }

    @Test
    void atualizarStatus_QuandoTarefaNaoExiste_NaoDeveLancarExcecao() {

        assertDoesNotThrow(() -> {
            tarefaRepository.atualizarStatus(999L, Status.TO_DO);
        });

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM tarefa", Integer.class);
        assertEquals(0, count);
    }

    @Test
    void atualizarStatus_QuandoOcorreErro_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE tarefa");

        ApiException exception = assertThrows(ApiException.class, () -> {
            tarefaRepository.atualizarStatus(1L, Status.TO_DO);
        });

        assertEquals("Erro ao atualizar o status da tarefa.", exception.getMessage());
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, exception.getStatus());
    }

    @Test
    void atualizarTarefa_QuandoDadosValidos_DeveAtualizarRegistro() {
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = 'Tarefa 1 User 1'", Long.class);

        Tarefa tarefaAtualizada = new Tarefa();
        tarefaAtualizada.setId(idTarefa);
        tarefaAtualizada.setDescricao("Nova descrição");
        tarefaAtualizada.setPrioridade(Prioridade.BAIXA);
        tarefaAtualizada.setStatus(Status.DONE);

        tarefaRepository.atualizarTarefa(tarefaAtualizada);

        Map<String, Object> tarefaDb = jdbcTemplate.queryForMap(
                "SELECT * FROM tarefa WHERE id = ?", idTarefa);

        assertEquals("Nova descrição", tarefaDb.get("descricao"));
        assertEquals("BAIXA", tarefaDb.get("prioridade"));
        assertEquals("DONE", tarefaDb.get("status"));
        assertEquals(1L, tarefaDb.get("usuario_id"));
    }

    @Test
    void atualizarTarefa_ComStatusDONE_DeveAtualizarCorretamente() {
        // Arrange
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE descricao = 'Tarefa 2 User 1'", Long.class);

        Tarefa tarefaAtualizada = new Tarefa();
        tarefaAtualizada.setId(idTarefa);
        tarefaAtualizada.setDescricao("Tarefa concluída");
        tarefaAtualizada.setPrioridade(Prioridade.BAIXA);
        tarefaAtualizada.setStatus(Status.DONE);

        // Act
        tarefaRepository.atualizarTarefa(tarefaAtualizada);

        // Assert
        Map<String, Object> tarefaDb = jdbcTemplate.queryForMap(
                "SELECT * FROM tarefa WHERE id = ?", idTarefa);

        assertEquals("Tarefa concluída", tarefaDb.get("descricao"));
        assertEquals("BAIXA", tarefaDb.get("prioridade"));
        assertEquals("DONE", tarefaDb.get("status"));
    }

    @ParameterizedTest
    @EnumSource(Status.class)
    void atualizarStatus_DeveFuncionarParaTodosStatus(Status status) {
        // Arrange
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE status = 'TO_DO' LIMIT 1", Long.class);

        // Act
        tarefaRepository.atualizarStatus(idTarefa, status);

        // Assert
        String statusAtual = jdbcTemplate.queryForObject(
                "SELECT status FROM tarefa WHERE id = ?",
                String.class,
                idTarefa);
        assertEquals(status.name(), statusAtual);
    }
    @Test
    void atualizarStatus_DeTO_DOParaDOING_DeveAtualizarCorretamente() {
        // Arrange
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE status = 'TO_DO' LIMIT 1", Long.class);

        // Act
        tarefaRepository.atualizarStatus(idTarefa, Status.DOING);

        // Assert
        String statusAtual = jdbcTemplate.queryForObject(
                "SELECT status FROM tarefa WHERE id = ?",
                String.class,
                idTarefa);
        assertEquals("DOING", statusAtual);
    }

    @Test
    void atualizarStatus_DeDOINGParaDONE_DeveAtualizarCorretamente() {
        // Arrange
        inserirDadosTeste();
        Long idTarefa = jdbcTemplate.queryForObject(
                "SELECT id FROM tarefa WHERE status = 'DOING' LIMIT 1", Long.class);

        // Act
        tarefaRepository.atualizarStatus(idTarefa, Status.DONE);

        // Assert
        String statusAtual = jdbcTemplate.queryForObject(
                "SELECT status FROM tarefa WHERE id = ?",
                String.class,
                idTarefa);
        assertEquals("DONE", statusAtual);
    }


}
