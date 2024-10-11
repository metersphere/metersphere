package io.metersphere.system.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

import java.io.IOException;
import java.text.DecimalFormat;

public class CustomRateSerializer extends JsonSerializer<Double> {

	private final DecimalFormat format = new DecimalFormat("0.00");

	@Override
	public void serialize(Double value, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
		if (value != null) {
			jsonGenerator.writeString(format.format(value));
		}
	}
}
