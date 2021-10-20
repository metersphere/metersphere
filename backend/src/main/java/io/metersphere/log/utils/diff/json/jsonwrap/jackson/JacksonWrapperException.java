package io.metersphere.log.utils.diff.json.jsonwrap.jackson;

@SuppressWarnings("serial")
public class JacksonWrapperException extends RuntimeException {

	public JacksonWrapperException() {
		super();
	}

	public JacksonWrapperException(String message, Throwable cause) {
		super(message, cause);
	}

	public JacksonWrapperException(String message) {
		super(message);
	}

	public JacksonWrapperException(Throwable cause) {
		super(cause);
	}

}
