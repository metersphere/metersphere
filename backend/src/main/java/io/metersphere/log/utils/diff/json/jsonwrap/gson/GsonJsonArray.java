package io.metersphere.log.utils.diff.json.jsonwrap.gson;

import java.lang.reflect.Field;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import io.metersphere.log.utils.diff.json.jsonwrap.JzonArray;
import io.metersphere.log.utils.diff.json.jsonwrap.JzonElement;

public class GsonJsonArray extends GsonJsonElement implements JzonArray {

	private final JsonArray wrapped;

	public GsonJsonArray(JsonArray wrapped) {
		super(wrapped);
		this.wrapped = wrapped;
	}

	@Override
	public int size() {
		return wrapped.size();
	}

	@Override
	public JzonElement get(int index) {
		return GsonWrapper.wrap(wrapped.get(index));
	}

	@Override
	public void insert(int index, JzonElement el) {
		getElements(wrapped).add(index, (JsonElement) el.unwrap());
	}

	@Override
	public void set(int index, JzonElement el) {
		getElements(wrapped).set(index, (JsonElement) el.unwrap());
	}

	@Override
	public void remove(int index) {
		getElements(wrapped).remove(index);
	}

	@Override
	public String toString() {
		return wrapped.toString();
	}

	private final static Field JsonArray_elements;

	static {

		try {
			JsonArray_elements = JsonArray.class.getDeclaredField("elements");
			JsonArray_elements.setAccessible(true);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	@SuppressWarnings("unchecked")
	private static ArrayList<JsonElement> getElements(JsonArray arr) {

		try {
			return (ArrayList<JsonElement>) JsonArray_elements.get(arr);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

}
