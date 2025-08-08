package com.carlos.todoapi.repository;

import com.carlos.todoapi.domain.bean.Tarefa;
import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.domain.enums.Status;
import com.carlos.todoapi.exception.ApiException;

import java.sql.PreparedStatement;
import java.util.List;
import java.util.Optional;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

@Repository
public class TarefaRepository {

    private final JdbcTemplate jdbcTemplate;

    public TarefaRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    private final RowMapper<Tarefa> rowMapper = (rs, rowNum) -> {
        Tarefa tarefa = new Tarefa();
        tarefa.setId(rs.getLong("id"));
        tarefa.setDescricao(rs.getString("descricao"));
        tarefa.setPrioridade(Prioridade.valueOf(rs.getString("prioridade")));
        tarefa.setStatus(Status.valueOf(rs.getString("status")));
        tarefa.setUsuarioId(rs.getLong("usuario_id"));
        return tarefa;
    };

    public Tarefa salvar(Tarefa tarefa) {
        String sql = "INSERT INTO tarefa (descricao, prioridade, status, usuario_id) VALUES (?, ?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        try {
            jdbcTemplate.update(connection -> {
                PreparedStatement ps = connection.prepareStatement(sql, new String[]{"id"});
                ps.setString(1, tarefa.getDescricao());
                ps.setString(2, tarefa.getPrioridade().name());
                ps.setString(3, tarefa.getStatus().name());
                ps.setLong(4, tarefa.getUsuarioId());
                return ps;
            }, keyHolder);

            Number id = keyHolder.getKey();
            if (id != null) {
                tarefa.setId(id.longValue());
            }

            return tarefa;
        } catch (Exception e) {
            throw new ApiException("Erro ao salvar a tarefa no banco de dados.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public Optional<Tarefa> buscarPorId(Long id) {
        String sql = "SELECT * FROM tarefa WHERE id = ?";
        try {
            List<Tarefa> tarefas = jdbcTemplate.query(sql, rowMapper, id);
            return tarefas.stream().findFirst();
        } catch (Exception e) {
            throw new ApiException("Erro ao buscar a tarefa por ID.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<Tarefa> buscarPorIdEUsuarioId(Long id, Long usuarioId) {
        String sql = "SELECT * FROM tarefa WHERE id = ? AND usuario_id = ?";
        try {
            List<Tarefa> tarefas = jdbcTemplate.query(sql, rowMapper, id, usuarioId);
            return tarefas.stream().findFirst();
        } catch (Exception e) {
            throw new ApiException("Erro ao buscar a tarefa por ID e usuário.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void deletar(Long id) {
        String sql = "DELETE FROM tarefa WHERE id = ?";
        try {
            jdbcTemplate.update(sql, id);
        } catch (Exception e) {
            throw new ApiException("Erro ao deletar a tarefa.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    public List<Tarefa> buscarPorStatusEUsuarioId(Status status, Long usuarioId) {
        String sql = "SELECT * FROM tarefa WHERE status = ? AND usuario_id = ?";
        try {
            return jdbcTemplate.query(sql, rowMapper, status.name(), usuarioId);
        } catch (Exception e) {
            throw new ApiException("Erro ao buscar tarefas por status e usuário.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public List<Tarefa> buscarPorStatusEPrioridadeEUsuarioId(Status status, Prioridade prioridade, Long usuarioId) {
        String sql = "SELECT * FROM tarefa WHERE status = ? AND prioridade = ? AND usuario_id = ?";
        try {
            return jdbcTemplate.query(sql, rowMapper, status.name(), prioridade.name(), usuarioId);
        } catch (Exception e) {
            throw new ApiException("Erro ao buscar tarefas por status, prioridade e usuário.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void atualizarStatus(Long id, Status status) {
        String sql = "UPDATE tarefa SET status = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql, status.name(), id);
        } catch (Exception e) {
            throw new ApiException("Erro ao atualizar o status da tarefa.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void atualizarTarefa(Tarefa tarefa) {
        String sql = "UPDATE tarefa SET descricao = ?, prioridade = ?, status = ? WHERE id = ?";
        try {
            jdbcTemplate.update(sql,
                    tarefa.getDescricao(),
                    tarefa.getPrioridade().name(),
                    tarefa.getStatus().name(),
                    tarefa.getId());
        } catch (Exception e) {
            throw new ApiException("Erro ao atualizar a tarefa.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
