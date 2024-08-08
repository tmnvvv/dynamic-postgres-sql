package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TitleDescription implements Serializable {

    private static final long serialVersionUID = 5258099550415190038L;

    private String text;
}
