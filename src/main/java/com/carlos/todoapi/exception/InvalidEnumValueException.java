package com.carlos.todoapi.exception;

import lombok.Data;

@Data
public class InvalidEnumValueException extends RuntimeException {
    private final String enumType;
    private final String invalidValue;

    public InvalidEnumValueException(String enumType, String invalidValue) {
        super(String.format("Valor '%s' inválido para o enum %s", invalidValue, enumType));
        this.enumType = enumType;
        this.invalidValue = invalidValue;
    }


}