package io.metersphere.log.utils.json.diff;

import io.metersphere.log.utils.json.diff.jsonwrap.jackson.JacksonWrapper;

public class JacksonDiff extends JsonDiff {

	public JacksonDiff() {
		super(new JacksonWrapper());
	}

}
