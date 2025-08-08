package com.carlos.todoapi.dto;

import com.carlos.todoapi.domain.dto.usuario.LoginRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class LoginRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void quandoDadosValidos_entaoNenhumaViolacao() {
        LoginRequest request = new LoginRequest("email@valido.com", "Senha@123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void quandoEmailVazio_entaoViolacaoNotBlank() {
        LoginRequest request = new LoginRequest("", "Senha@123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("O e-mail é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoEmailNulo_entaoViolacaoNotBlank() {
        LoginRequest request = new LoginRequest(null, "Senha@123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("O e-mail é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoEmailInvalido_entaoViolacaoEmail() {
        LoginRequest request = new LoginRequest("email-invalido", "Senha@123");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("E-mail inválido.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoSenhaVazia_entaoViolacaoNotBlank() {
        LoginRequest request = new LoginRequest("email@valido.com", "");
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("A senha é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoSenhaNula_entaoViolacaoNotBlank() {
        LoginRequest request = new LoginRequest("email@valido.com", null);
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("A senha é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoTodosCamposInvalidos_entaoMultiplasViolacoes() {
        LoginRequest request = new LoginRequest("", null);
        Set<ConstraintViolation<LoginRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("O e-mail é obrigatório.")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("A senha é obrigatória.")));
    }

    @Test
    void quandoInstanciaComConstrutorVazio_entaoCamposNulos() {
        LoginRequest request = new LoginRequest();
        assertNull(request.getEmail());
        assertNull(request.getSenha());
    }

    @Test
    void quandoInstanciaComConstrutorCompleto_entaoCamposPreenchidos() {
        LoginRequest request = new LoginRequest("teste@email.com", "senha123");
        assertEquals("teste@email.com", request.getEmail());
        assertEquals("senha123", request.getSenha());
    }
}
