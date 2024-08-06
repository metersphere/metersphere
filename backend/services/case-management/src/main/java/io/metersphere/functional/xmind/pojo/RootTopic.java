package io.metersphere.functional.xmind.pojo;

import lombok.Data;

import java.util.List;

@Data
public class RootTopic {

	private String id;
	private String title;
	private Notes notes;
	private List<Comments> comments;
	private Children children;

}
