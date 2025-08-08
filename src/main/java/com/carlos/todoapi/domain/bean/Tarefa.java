/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.bean;

import com.carlos.todoapi.domain.enums.Prioridade;
import com.carlos.todoapi.domain.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 *
 * @author carlos.lacerda
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarefa {

    private Long id;
    private String descricao;
    private Prioridade prioridade;
    private Long usuarioId;
    private Status status;
}
