package com.carlos.todoapi.dto;

import com.carlos.todoapi.domain.dto.usuario.UsuarioRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class UsuarioRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void quandoDadosValidos_entaoNenhumaViolacao() {
        UsuarioRequest request = new UsuarioRequest("Nome Completo", "email@valido.com", "Senha@123");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void quandoNomeVazio_entaoViolacaoNotBlank() {
        UsuarioRequest request = new UsuarioRequest("", "email@valido.com", "Senha@123");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("O nome é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoNomeNulo_entaoViolacaoNotBlank() {
        UsuarioRequest request = new UsuarioRequest(null, "email@valido.com", "Senha@123");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("O nome é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoEmailVazio_entaoViolacaoNotBlank() {
        UsuarioRequest request = new UsuarioRequest("Nome", "", "Senha@123");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("O e-mail é obrigatório.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoEmailInvalido_entaoViolacaoEmail() {
        UsuarioRequest request = new UsuarioRequest("Nome", "email-invalido", "Senha@123");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("E-mail inválido.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoSenhaVazia_entaoViolacaoNotBlank() {
        UsuarioRequest request = new UsuarioRequest("Nome", "email@valido.com", "");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("A senha é obrigatória.")));
    }

    @Test
    void quandoSenhaNula_entaoViolacaoNotBlank() {
        UsuarioRequest request = new UsuarioRequest("Nome", "email@valido.com", null);
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        assertFalse(violations.isEmpty());
        assertTrue(violations.stream().anyMatch(v ->
                v.getMessage().equals("A senha é obrigatória.")));
    }

    @Test
    void quandoSenhaFraca_entaoViolacaoPattern() {
        // Teste para cada tipo de senha inválida
        testarSenhaInvalida("somenteMinusculas", "Falta maiúscula, número e caractere especial");
        testarSenhaInvalida("SEMNUMEROS@", "Falta número e letra minúscula");
        testarSenhaInvalida("12345678!", "Falta letra maiúscula e minúscula");
        testarSenhaInvalida("Senha123", "Falta caractere especial");

    }

    private void testarSenhaInvalida(String senhaInvalida, String mensagemErro) {
        UsuarioRequest request = new UsuarioRequest("Nome", "email@valido.com", senhaInvalida);
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);

        if (violations.isEmpty()) {
            fail("Deveria falhar para: " + mensagemErro + " - Senha: " + senhaInvalida);
        }

        ConstraintViolation<UsuarioRequest> violacao = violations.iterator().next();
        assertEquals("senha", violacao.getPropertyPath().toString());
        assertTrue(
                violacao.getMessage().startsWith("Senha não corresponde aos requisitos mínimos de segurança"),
                "Mensagem de validação incorreta para: " + mensagemErro
        );
    }



    @Test
    void quandoSenhaValida_entaoNenhumaViolacao() {
        String[] senhasValidas = {
                "Senha@123",
                "P@ssw0rd",
                "A1b2C3d4!",
                "Teste123$",
                "Outr@Senha99"
        };

        for (String senha : senhasValidas) {
            UsuarioRequest request = new UsuarioRequest("Nome", "email@valido.com", senha);
            Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
            assertTrue(violations.isEmpty(), "Falha na senha: " + senha);
        }
    }

    @Test
    void quandoInstanciaComConstrutorVazio_entaoCamposNulos() {
        UsuarioRequest request = new UsuarioRequest();
        assertNull(request.getNome());
        assertNull(request.getEmail());
        assertNull(request.getSenha());
    }

    @Test
    void quandoInstanciaComConstrutorCompleto_entaoCamposPreenchidos() {
        UsuarioRequest request = new UsuarioRequest("Nome", "email@teste.com", "Senha@123");
        assertEquals("Nome", request.getNome());
        assertEquals("email@teste.com", request.getEmail());
        assertEquals("Senha@123", request.getSenha());
    }

    @Test
    void quandoTodosCamposInvalidos_entaoMultiplasViolacoes() {
        UsuarioRequest request = new UsuarioRequest("", "invalido", "fraca");
        Set<ConstraintViolation<UsuarioRequest>> violations = validator.validate(request);
        assertEquals(3, violations.size());
    }
}
