package ru.dynamic.utils;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CustomStatementParameter {
    private Object value;
    private int type;
}
