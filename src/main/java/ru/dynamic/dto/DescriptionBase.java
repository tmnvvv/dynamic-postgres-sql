package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DescriptionBase implements Serializable {

	private static final long serialVersionUID = -5632079572873483899L;

	private String id;

	private String name;
}
