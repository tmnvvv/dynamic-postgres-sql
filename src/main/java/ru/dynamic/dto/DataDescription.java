package ru.dynamic.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Map;

@Data
@EqualsAndHashCode(callSuper=true)
public class DataDescription extends DescriptionBase {

	private static final long serialVersionUID = -3721585390444569981L;

	private DescriptionSource source;

	private TitleDescription title;
	
	private Map<String, DescriptionSource> operations;

}
