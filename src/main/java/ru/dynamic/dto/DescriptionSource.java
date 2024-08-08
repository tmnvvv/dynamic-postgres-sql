package ru.dynamic.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class DescriptionSource implements Serializable {

	private static final long serialVersionUID = 2451401052532045429L;

	private String postgresQuery;

	private List<DescriptionResultDataItem> resultData;

	private List<RequestParam> params;

}
