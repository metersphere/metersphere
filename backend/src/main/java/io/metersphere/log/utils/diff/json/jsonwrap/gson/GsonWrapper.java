package io.metersphere.log.utils.diff.json.jsonwrap.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

import io.metersphere.log.utils.diff.json.jsonwrap.Wrapper;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonElement;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonArray;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonObject;

public class GsonWrapper implements Wrapper {

	private final static JsonParser JSON = new JsonParser();

	public static JzonElement wrap(JsonElement el) {
		if (el == null || el.isJsonNull()) {
			return GsonJsonNull.INSTANCE;
		} else if (el.isJsonArray()) {
			return new GsonJsonArray((JsonArray) el);
		} else if (el.isJsonObject()) {
			return new GsonJsonObject((JsonObject) el);
		} else if (el.isJsonPrimitive()) {
			return new GsonJsonPrimitive((JsonPrimitive) el);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public JzonElement parse(String json) {
		return wrap(JSON.parse(json));
	}

	@Override
	public JzonElement wrap(Object o) {
		return wrap((JsonElement) o);
	}

	@Override
	public JzonObject createJsonObject() {
		return (JzonObject) wrap(new JsonObject());
	}

	@Override
	public JzonArray createJsonArray() {
		return (JzonArray) wrap(new JsonArray());
	}

}
