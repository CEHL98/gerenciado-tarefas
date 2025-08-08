/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.carlos.todoapi.domain.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 *
 * @author Particular
 */
@AllArgsConstructor
@Getter
public class ErrorResponse {
    private int status;
    private String message;
}
