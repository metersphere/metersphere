package io.metersphere.log.utils.json.diff.jsonwrap.jackson2;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonArray;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonElement;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonObject;
import io.metersphere.log.utils.json.diff.jsonwrap.Wrapper;

import java.io.IOException;

public class Jackson2Wrapper implements Wrapper {

	private final static ObjectMapper JSON = new ObjectMapper();

	public static JzonElement wrap(JsonNode el) {
		if (el == null || el.isNull()) {
			return Jackson2JsonNull.INSTANCE;
		} else if (el.isArray()) {
			return new Jackson2JsonArray((ArrayNode) el);
		} else if (el.isObject()) {
			return new Jackson2JsonObject((ObjectNode) el);
		} else if (el.isValueNode()) {
			return new Jackson2JsonPrimitive((ValueNode) el);
		} else {
			throw new IllegalStateException();
		}
	}

	@Override
	public JzonElement parse(String json) {
		try {
			JsonParser parser = JSON.getJsonFactory().createJsonParser(json);
			parser.configure(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES, true);
			parser.configure(JsonParser.Feature.ALLOW_SINGLE_QUOTES, true);
			return wrap(parser.readValueAsTree());
		} catch (JsonProcessingException e) {
			throw new Jackson2WrapperException("Failed to parse JSON", e);
		} catch (IOException e) {
			throw new Jackson2WrapperException("IOException parsing a String?", e);
		}
	}

	@Override
	public JzonElement wrap(Object o) {
		return wrap((JsonNode) o);
	}

	@Override
	public JzonObject createJsonObject() {
		return (JzonObject) wrap(JSON.createObjectNode());
	}

	@Override
	public JzonArray createJsonArray() {
		return (JzonArray) wrap(JSON.createArrayNode());
	}

}
