package com.carlos.todoapi.dto;

import com.carlos.todoapi.domain.dto.tarefa.AtualizarTarefaRequest;
import com.carlos.todoapi.domain.enums.Prioridade;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.*;

class AtualizarTarefaRequestTest {

    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void quandoDescricaoValidaEPrioridadeValida_entaoNenhumaViolacao() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("Descrição válida");
        request.setPrioridade(Prioridade.ALTA);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertTrue(violations.isEmpty());
    }

    @Test
    void quandoDescricaoVazia_entaoViolacaoNotBlank() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("");
        request.setPrioridade(Prioridade.MEDIA);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("A descrição não pode estar vazia", violations.iterator().next().getMessage());
    }

    @Test
    void quandoDescricaoNula_entaoViolacaoNotBlank() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao(null);
        request.setPrioridade(Prioridade.BAIXA);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("A descrição não pode estar vazia", violations.iterator().next().getMessage());
    }

    @Test
    void quandoDescricaoMuitoLonga_entaoViolacaoSize() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("a".repeat(256));
        request.setPrioridade(Prioridade.ALTA);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("A descrição deve ter no máximo 255 caracteres", violations.iterator().next().getMessage());
    }

    @Test
    void quandoPrioridadeNula_entaoViolacaoNotNull() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("Descrição válida");
        request.setPrioridade(null);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertEquals(1, violations.size());
        assertEquals("A prioridade é obrigatória", violations.iterator().next().getMessage());
    }

    @Test
    void quandoTodosCamposInvalidos_entaoMultiplasViolacoes() {
        AtualizarTarefaRequest request = new AtualizarTarefaRequest();
        request.setDescricao("");
        request.setPrioridade(null);

        Set<ConstraintViolation<AtualizarTarefaRequest>> violations = validator.validate(request);

        assertEquals(2, violations.size());
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("A descrição não pode estar vazia")));
        assertTrue(violations.stream().anyMatch(v -> v.getMessage().equals("A prioridade é obrigatória")));
    }
}