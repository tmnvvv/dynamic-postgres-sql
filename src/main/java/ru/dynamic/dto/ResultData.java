package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class ResultData implements Serializable {

    private static final long serialVersionUID = 4710785287471546581L;

    private Object result;
}
