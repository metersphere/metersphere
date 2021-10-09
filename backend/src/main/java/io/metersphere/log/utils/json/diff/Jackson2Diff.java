package io.metersphere.log.utils.json.diff;

import io.metersphere.log.utils.json.diff.jsonwrap.jackson2.Jackson2Wrapper;

public class Jackson2Diff extends JsonDiff {

	public Jackson2Diff() {
		super(new Jackson2Wrapper());
	}

}
