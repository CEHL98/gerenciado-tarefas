package com.carlos.todoapi.dto;

import com.carlos.todoapi.domain.dto.tarefa.TarefaRequest;
import com.carlos.todoapi.domain.enums.Prioridade;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class TarefaRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void quandoDadosValidos_entaoNenhumaViolacao() {
        TarefaRequest request = new TarefaRequest("Descrição válida", Prioridade.ALTA);
        Set<ConstraintViolation<TarefaRequest>> violations = validator.validate(request);
        assertTrue(violations.isEmpty());
    }

    @Test
    void quandoDescricaoVazia_entaoViolacaoNotBlank() {
        TarefaRequest request = new TarefaRequest("", Prioridade.MEDIA);
        Set<ConstraintViolation<TarefaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("A descrição é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoDescricaoNula_entaoViolacaoNotBlank() {
        TarefaRequest request = new TarefaRequest(null, Prioridade.BAIXA);
        Set<ConstraintViolation<TarefaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("A descrição é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoPrioridadeNula_entaoViolacaoNotNull() {
        TarefaRequest request = new TarefaRequest("Descrição válida", null);
        Set<ConstraintViolation<TarefaRequest>> violations = validator.validate(request);
        assertEquals(1, violations.size());
        assertEquals("A prioridade é obrigatória.", violations.iterator().next().getMessage());
    }

    @Test
    void quandoTodosCamposInvalidos_entaoMultiplasViolacoes() {
        TarefaRequest request = new TarefaRequest(null, null);
        Set<ConstraintViolation<TarefaRequest>> violations = validator.validate(request);
        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("A descrição é obrigatória.")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("A prioridade é obrigatória.")));
    }

    @Test
    void quandoInstanciaComConstrutorVazio_entaoCamposNulos() {
        TarefaRequest request = new TarefaRequest();
        assertNull(request.getDescricao());
        assertNull(request.getPrioridade());
    }

    @Test
    void quandoInstanciaComConstrutorCompleto_entaoCamposPreenchidos() {
        TarefaRequest request = new TarefaRequest("Descrição", Prioridade.ALTA);
        assertEquals("Descrição", request.getDescricao());
        assertEquals(Prioridade.ALTA, request.getPrioridade());
    }
}
