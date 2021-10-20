package io.metersphere.log.utils.diff.json;

import io.metersphere.log.utils.diff.json.jsonwrap.jackson.JacksonWrapper;

public class JacksonDiff extends JsonDiff {

	public JacksonDiff() {
		super(new JacksonWrapper());
	}

}
