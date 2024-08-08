
package io.metersphere.functional.xmind.pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * XMind 节点对象
 */
@Setter
@Getter
public class Attached {

	private String id;
	private String title;
	private Notes notes;
	private String path;
	private Attached parent;
	private List<Comments> comments;
	private Children children;

}
