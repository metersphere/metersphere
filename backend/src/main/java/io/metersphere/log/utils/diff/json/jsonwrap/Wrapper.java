package io.metersphere.log.utils.diff.json.jsonwrap;

/**
 * Factory wrapper interface for multiple json implementations.
 * 
 * @since 1.0.0
 */
public interface Wrapper {

	/**
	 * Parses an element given a string.
	 * 
	 * @param json
	 *            - string
	 * @return parsed element
	 */
	JzonElement parse(String json);

	/**
	 * Wraps a given json element.
	 * 
	 * @param o
	 *            - element to wrap
	 * @return the wrapped element
	 */
	JzonElement wrap(Object o);

	/**
	 * @return a new implementation independent json object
	 */
	JzonObject createJsonObject();

	/**
	 * @return a new implementation independent json array
	 */
	JzonArray createJsonArray();

}
