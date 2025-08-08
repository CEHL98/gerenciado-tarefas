package com.carlos.todoapi.repository;

import com.carlos.todoapi.domain.bean.Usuario;
import com.carlos.todoapi.exception.ApiException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.ANY)
public class UsuarioRepositoryTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @BeforeEach
    public void setup() {
        jdbcTemplate.execute("DROP TABLE IF EXISTS tarefa");
        jdbcTemplate.execute("DROP TABLE IF EXISTS usuario");

        jdbcTemplate.execute("CREATE TABLE usuario(" +
                "id BIGINT AUTO_INCREMENT PRIMARY KEY, " +
                "nome VARCHAR(255), " +
                "email VARCHAR(255) UNIQUE, " +
                "senha VARCHAR(255))");
    }


    @Test
    public void testSave_DeveSalvarUsuarioComSucesso() {

        Usuario usuario = new Usuario(null, "Maria Silva", "maria@email.com", "senha123");

        usuarioRepository.save(usuario);

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM usuario WHERE email = ?",
                Integer.class,
                "maria@email.com"
        );

        assertEquals(1, count, "Deveria haver exatamente 1 usuário com este email");

        Usuario usuarioSalvo = jdbcTemplate.queryForObject(
                "SELECT * FROM usuario WHERE email = ?",
                (rs, rowNum) -> new Usuario(
                        rs.getLong("id"),
                        rs.getString("nome"),
                        rs.getString("email"),
                        rs.getString("senha")
                ),
                "maria@email.com"
        );
        assertNotNull(usuarioSalvo.getId(), "ID deveria ser gerado automaticamente");
        assertEquals("Maria Silva", usuarioSalvo.getNome());
        assertEquals("senha123", usuarioSalvo.getSenha());
    }

    @Test
    public void testFindByEmail_QuandoUsuarioExiste_DeveRetornarUsuario() {
        jdbcTemplate.update(
                "INSERT INTO usuario (nome, email, senha) VALUES (?, ?, ?)",
                "João Souza", "joao@email.com", "senha456"
        );

        Optional<Usuario> resultado = usuarioRepository.findByEmail("joao@email.com");

        assertTrue(resultado.isPresent(), "Deveria encontrar o usuário");
        assertEquals("João Souza", resultado.get().getNome());
        assertEquals("senha456", resultado.get().getSenha());
    }

    @Test
    public void testFindByEmail_QuandoUsuarioNaoExiste_DeveRetornarOptionalVazio() {
        Optional<Usuario> resultado = usuarioRepository.findByEmail("inexistente@email.com");

        assertFalse(resultado.isPresent(), "Não deveria encontrar usuário");
    }

    @Test
    public void testSave_QuandoOcorreErroNoBanco_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE usuario");

        assertThrows(ApiException.class, () -> {
            usuarioRepository.save(new Usuario(null, "Teste", "teste@email.com", "senha"));
        });
    }

    @Test
    public void testFindByEmail_QuandoOcorreErroNoBanco_DeveLancarApiException() {
        jdbcTemplate.execute("DROP TABLE usuario");

        assertThrows(ApiException.class, () -> {
            usuarioRepository.findByEmail("qualquer@email.com");
        });
    }
}
