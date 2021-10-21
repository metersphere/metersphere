package io.metersphere.log.utils.diff.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import io.metersphere.log.utils.diff.json.jsonwrap.gson.GsonWrapper;

public class GsonDiff extends JsonDiff {

	public GsonDiff() {
		super(new GsonWrapper());
	}

	public JsonObject diff(JsonElement from, JsonElement to) throws IllegalArgumentException {
		return (JsonObject) super.diff(from, to);
	}
}
