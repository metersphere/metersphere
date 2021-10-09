package io.metersphere.log.utils.json.diff.jsonwrap.jackson2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonArray;
import io.metersphere.log.utils.json.diff.jsonwrap.JzonElement;

public class Jackson2JsonArray extends Jackson2JsonElement implements JzonArray {

	private final ArrayNode wrapped;

	public Jackson2JsonArray(ArrayNode wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public JzonElement get(int index) {
		return Jackson2Wrapper.wrap(wrapped.get(index));
	}

	@Override
	public void insert(int index, JzonElement el) {
		wrapped.insert(index, (JsonNode) el.unwrap());
	}

	@Override
	public void set(int index, JzonElement el) {
		wrapped.set(index, (JsonNode) el.unwrap());
	}

	@Override
	public void remove(int index) {
		wrapped.remove(index);
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

}
