package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class DescriptionResultDataItem implements Serializable {

	private static final long serialVersionUID = -1383229241121410192L;

	private String source;

	private String target;
	
	private String type;
}
