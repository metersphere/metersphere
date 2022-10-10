package io.metersphere.log.utils.diff.json.jsonwrap.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.ValueNode;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonArray;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonElement;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonObject;
import io.metersphere.log.utils.diff.json.jsonwrap.Wrapper;

import java.io.IOException;

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
