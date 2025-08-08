/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.repository;

import com.carlos.todoapi.domain.bean.Usuario;
import java.util.Optional;

import com.carlos.todoapi.exception.ApiException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;

/**
 *
 * @author Particular
 */
@Repository
public class UsuarioRepository {

    private final JdbcTemplate jdbcTemplate;

    public UsuarioRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }
    
    
    private final RowMapper<Usuario> usuarioRowMapper = (rs, rowNum)
            -> new Usuario(
                    rs.getLong("id"),
                    rs.getString("nome"),
                    rs.getString("email"),
                    rs.getString("senha")
            );

    public Optional<Usuario> findByEmail(String email) {
        String sql = "SELECT * FROM usuario WHERE email = ?";
        try {
            Usuario usuario = jdbcTemplate.queryForObject(sql, usuarioRowMapper, email);
            return Optional.ofNullable(usuario);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        } catch (Exception e) {
            throw new ApiException("Houve um erro ao buscar o usuário no banco de dados.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    public void save(Usuario usuario) {
        String sql = "INSERT INTO usuario (nome, email, senha) VALUES (?, ?, ?)";
        try {
            jdbcTemplate.update(sql, usuario.getNome(), usuario.getEmail(), usuario.getSenha());
        } catch (Exception e) {
            throw new ApiException("Houve um erro ao salvar o usuário no banco de dados.", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
