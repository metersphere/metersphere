package io.metersphere.api.dubbo.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Type;

/**
 * JsonUtils
 */
public class JsonUtils {

	private static final Logger logger = LoggerFactory.getLogger(JsonUtils.class);

	private static final Gson gson = new GsonBuilder()
			.setDateFormat("yyyy-MM-dd HH:mm:ss")
			.setPrettyPrinting()
			.disableHtmlEscaping()
			.serializeNulls()
			.create();

	public static String toJson(Object obj) {
		return gson.toJson(obj);
	}

	public static String toJson(Object obj, Type type) {
		return gson.toJson(obj, type);
	}

	public static <T> T formJson(String json, Class<T> classOfT) {
		try {
			return gson.fromJson(json, classOfT);
		} catch (JsonSyntaxException e) {
			logger.error("json to class[" + classOfT.getName() + "] is error!",
					e);
		}
		return null;
	}

	public static <T> T formJson(String json, Type type) {
		try {
			return gson.fromJson(json, type);
		} catch (JsonSyntaxException e) {
			logger.error("json to class[" + type.getClass().getName()
					+ "] is error!", e);
		}
		return null;
	}
}
