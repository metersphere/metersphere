package io.metersphere.log.utils.diff.json.jsonwrap.jackson;

import java.io.IOException;

import io.metersphere.log.utils.diff.json.jsonwrap.Wrapper;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.codehaus.jackson.node.ValueNode;

import io.metersphere.log.utils.diff.json.jsonwrap.JzonArray;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonElement;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonObject;

public class JacksonWrapper implements Wrapper {

	private final static ObjectMapper JSON = new ObjectMapper();

	public static JzonElement wrap(JsonNode el) {
		if (el == null || el.isNull()) {
			return JacksonJsonNull.INSTANCE;
		} else if (el.isArray()) {
			return new JacksonJsonArray((ArrayNode) el);
		} else if (el.isObject()) {
			return new JacksonJsonObject((ObjectNode) el);
		} else if (el.isValueNode()) {
			return new JacksonJsonPrimitive((ValueNode) el);
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
			throw new JacksonWrapperException("Failed to parse JSON", e);
		} catch (IOException e) {
			throw new JacksonWrapperException("IOException parsing a String?", e);
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
