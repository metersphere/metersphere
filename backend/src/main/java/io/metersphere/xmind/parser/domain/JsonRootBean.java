package io.metersphere.xmind.parser.domain;

import lombok.Data;

@Data
public class JsonRootBean {

	private String id;
	private String title;
	private RootTopic rootTopic;

}