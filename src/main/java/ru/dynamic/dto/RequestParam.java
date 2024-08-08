package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RequestParam implements Serializable {

	private static final long serialVersionUID = 1446587724003153166L;

	private String requestName;

	private String type;
}
